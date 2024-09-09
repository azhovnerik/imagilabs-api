package com.anahoret.imagilabsapi.openai.domain

import arrow.core.toNonEmptyListOrNull
import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.students.storage.StudentProfileEntity
import com.anahoret.imagilabsapi.students.storage.StudentProfileEntityRepository
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntity
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntityRepository
import com.anahoret.imagilabsapi.users.UserType
import com.anahoret.imagilabsapi.utils.TimeZones
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
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
    private val classroomService: ClassroomService,
    @Value("\${spring.ai.openai.tip-tokens-per-hour}") private val tipTokens: Int,
    private val clock: Clock
) : TipTokensService {

    companion object {
        private val FREE_FOR_ALL_PERIOD_START = ZonedDateTime.of(2024, 9, 16, 0, 0, 0, 0, ZoneId.of("UTC-7")).toInstant().toEpochMilli()
        private val FREE_FOR_ALL_PERIOD_END = ZonedDateTime.of(2024, 10, 19, 0, 0, 0, 0, ZoneId.of("UTC-7")).toInstant().toEpochMilli()
        private val FREE_FOR_ALL_PERIOD = FREE_FOR_ALL_PERIOD_START..FREE_FOR_ALL_PERIOD_END
    }

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
        val teachersToUpdate = allTeachers
            .filter { teacherNeedsReplenish(now, it) }

        val studentsToUpdate = studentProfileEntityRepository.findAll().toNonEmptyListOrNull()?.let { allStudents ->
            val classroomsMap = classroomService.listByIds(allStudents.map(StudentProfileEntity::classroomId))
                .associateBy(Classroom::id)
            val teacherMap = allTeachers.associateBy(TeacherProfileEntity::id)
            val studentTeacherMap: Map<UUID, TeacherProfileEntity> = allStudents.associate {
                it.id!! to teacherMap.getValue(classroomsMap.getValue(it.classroomId).teacherId)
            }
            allStudents
                .filter { studentNeedsReplenish(now, it, studentTeacherMap.getValue(it.id!!)) }
        }

        teachersToUpdate.onEach { teacher ->
            teacher.tipTokens = tipTokens
            teacher.tipTokensReplenishedAt = now
        }.takeIf { it.isNotEmpty() }
            ?.let(teacherProfileEntityRepository::saveAll)

        studentsToUpdate?.onEach {
            it.tipTokens = tipTokens
            it.tipTokensReplenishedAt = now
        }.takeIf { !it.isNullOrEmpty() }
            ?.let(studentProfileEntityRepository::saveAll)
    }

    private fun teacherNeedsReplenish(now: Long, teacherProfileEntity: TeacherProfileEntity): Boolean {
        return now in FREE_FOR_ALL_PERIOD ||
                teacherProfileEntity.hasProSubscription(now) ||
                freePlanReplenishPeriodPassed(now, teacherProfileEntity.tipTokensReplenishedAt)
    }

    private fun studentNeedsReplenish(
        now: Long,
        studentProfileEntity: StudentProfileEntity,
        teacherProfileEntity: TeacherProfileEntity
    ): Boolean {
        return now in FREE_FOR_ALL_PERIOD ||
                teacherProfileEntity.hasProSubscription(now) ||
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
