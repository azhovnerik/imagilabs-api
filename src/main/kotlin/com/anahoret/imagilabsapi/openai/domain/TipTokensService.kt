package com.anahoret.imagilabsapi.openai.domain

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.students.storage.StudentProfileEntity
import com.anahoret.imagilabsapi.students.storage.StudentProfileEntityRepository
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntity
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntityRepository
import com.anahoret.imagilabsapi.userclassroomlink.domain.StudentClassroomLinkService
import com.anahoret.imagilabsapi.users.UserType
import com.anahoret.imagilabsapi.utils.TimeZones
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant
import java.time.ZonedDateTime
import java.util.*

interface TipTokensService {
    fun getTipTokens(userProfile: UserProfile): Int?
    fun replenishTipTokens()
    fun withdrawOneTipToken(userProfile: UserProfile)
    fun hasTipTokens(userProfile: UserProfile): Boolean?
}

@Service
class TipTokensServiceImpl(
    private val studentProfileEntityRepository: StudentProfileEntityRepository,
    private val teacherProfileEntityRepository: TeacherProfileEntityRepository,
    private val studentClassroomLinkService: StudentClassroomLinkService,
    @param:Value($$"${spring.ai.openai.tip-tokens-per-hour}") private val tipTokens: Int,
    private val clock: Clock
) : TipTokensService {

    override fun getTipTokens(userProfile: UserProfile): Int? {
        return when (userProfile.userType) {
            UserType.TEACHER -> teacherProfileEntityRepository.findByIdOrNull(userProfile.id)?.tipTokens
            UserType.STUDENT -> studentProfileEntityRepository.findByIdOrNull(userProfile.id)?.tipTokens
            UserType.ADMIN -> 0
        }
    }

    override fun replenishTipTokens() {
        val now = clock.millis()

        val allTeachers = teacherProfileEntityRepository.findAll()
        val teachersMap = allTeachers.associateBy { it.id!! }
        val teachersToUpdate = allTeachers
            .filter { teacherNeedsReplenish(now, it) }

        val studentsToUpdate = studentProfileEntityRepository.findAll()
            .filter { studentNeedsReplenish(now, it, teachersMap) }

        teachersToUpdate.onEach { teacher ->
            teacher.tipTokens = tipTokens
            teacher.tipTokensReplenishedAt = now
        }.takeIf { it.isNotEmpty() }
            ?.let(teacherProfileEntityRepository::saveAll)

        studentsToUpdate.onEach {
            it.tipTokens = tipTokens
            it.tipTokensReplenishedAt = now
        }.takeIf { it.isNotEmpty() }
            ?.let(studentProfileEntityRepository::saveAll)
    }

    private fun teacherNeedsReplenish(now: Long, teacherProfileEntity: TeacherProfileEntity): Boolean {
        return teacherProfileEntity.hasProSubscription(now) ||
                freePlanReplenishPeriodPassed(now, teacherProfileEntity.tipTokensReplenishedAt)
    }

    private fun studentNeedsReplenish(
        now: Long,
        studentProfileEntity: StudentProfileEntity,
        teachersMap: Map<UUID, TeacherProfileEntity>
    ): Boolean {
        val studentClassrooms = studentClassroomLinkService.listClassroomsByStudent(studentProfileEntity.id!!)
        val teachers = studentClassrooms.mapNotNull { teachersMap[it.teacherId] }
        return teachers.any { it.hasProSubscription(now) } ||
                freePlanReplenishPeriodPassed(now, studentProfileEntity.tipTokensReplenishedAt)
    }

    private fun freePlanReplenishPeriodPassed(now: Long, tipTokensReplenishedAt: Long): Boolean {
        val nowDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(now), TimeZones.UTC.toZoneId())
        val replenishDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(tipTokensReplenishedAt), TimeZones.UTC.toZoneId())
        val nextReplenishDate = replenishDate.plusMonths(1)
        return nowDate.isAfter(nextReplenishDate)
    }

    override fun withdrawOneTipToken(userProfile: UserProfile) {
        when (userProfile.userType) {
            UserType.TEACHER -> teacherProfileEntityRepository.findByIdOrNull(userProfile.id)?.let {
                it.tipTokens -= 1
                teacherProfileEntityRepository.save(it)
            }

            UserType.STUDENT -> studentProfileEntityRepository.findByIdOrNull(userProfile.id)?.let {
                it.tipTokens -= 1
                studentProfileEntityRepository.save(it)
            }

            UserType.ADMIN -> {}
        }

    }

    override fun hasTipTokens(userProfile: UserProfile): Boolean? {
        return when (userProfile.userType) {
            UserType.TEACHER -> teacherProfileEntityRepository.findByIdOrNull(userProfile.id)?.tipTokens?.let { it > 0 }
            UserType.STUDENT -> studentProfileEntityRepository.findByIdOrNull(userProfile.id)?.tipTokens?.let { it > 0 }
            UserType.ADMIN -> false
        }
    }
}
