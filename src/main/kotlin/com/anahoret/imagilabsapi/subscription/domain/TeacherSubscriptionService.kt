package com.anahoret.imagilabsapi.subscription.domain

import com.anahoret.imagilabsapi.classrooms.storage.ClassroomEntityRepository
import com.anahoret.imagilabsapi.openai.domain.OpenAiAccessService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntityRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Clock
import java.util.*

interface TeacherSubscriptionService {
    fun setPeriod(teacherId: UUID, start: Long, end: Long)
    fun cancelSubscription(teacherId: UUID)
    fun buildSubscriptionDto(teacherId: UUID, teacherSubscriptionData: TeacherSubscriptionData): TeacherSubscription
    fun canCreateClassroom(teacherProfile: TeacherProfile): Boolean
    fun studentLimitPerClassExceeded(teacherProfile: TeacherProfile, studentCountInClassroom: Long): Boolean
    fun getSubscriptionDto(teacherId: UUID): TeacherSubscription?
    fun getSubscriptionDtos(teacherIds: Set<UUID>): List<TeacherSubscription>
    fun hasProSubscription(teacherId: UUID): Boolean
}

@Service
class TeacherSubscriptionServiceImpl(
    private val teacherProfileEntityRepository: TeacherProfileEntityRepository,
    private val clock: Clock,
    private val classroomEntityRepository: ClassroomEntityRepository
) : TeacherSubscriptionService {
    override fun setPeriod(teacherId: UUID, start: Long, end: Long) {
        teacherProfileEntityRepository.findByIdOrNull(teacherId)
            ?.let {
                it.subscriptionStart = start
                it.subscriptionEnd = end
                it.subscriptionCanceled = false
                teacherProfileEntityRepository.save(it)
            }
    }

    override fun buildSubscriptionDto(
        teacherId: UUID,
        teacherSubscriptionData: TeacherSubscriptionData
    ): TeacherSubscription {
        with(teacherSubscriptionData) {
            val now = clock.instant().toEpochMilli()
            return if (hasProSubscription(now)) {
                TeacherSubscription(
                    subscriptionStart,
                    subscriptionEnd,
                    TeacherSubscriptionPlan.PRO,
                    subscriptionCanceled,
                    teacherId
                )
            } else {
                TeacherSubscription(
                    subscriptionStart,
                    subscriptionEnd,
                    TeacherSubscriptionPlan.STANDARD,
                    subscriptionCanceled,
                    teacherId
                )
            }
        }
    }

    override fun getSubscriptionDto(teacherId: UUID): TeacherSubscription? {
        return teacherProfileEntityRepository.findByIdOrNull(teacherId)
            ?.let { buildSubscriptionDto(teacherId, it) }
    }

    override fun getSubscriptionDtos(teacherIds: Set<UUID>): List<TeacherSubscription> {
        if (teacherIds.isEmpty()) return emptyList()

        val teachers = teacherProfileEntityRepository.findAllById(teacherIds)

        return teachers.map { teacher ->
            buildSubscriptionDto(teacher.id!!, teacher)
        }
    }

    override fun canCreateClassroom(teacherProfile: TeacherProfile): Boolean {
        val currentClassCount = classroomEntityRepository.countByTeacherId(teacherProfile.id)
        val maxClassrooms = when (teacherProfile.subscription.plan) {
            TeacherSubscriptionPlan.STANDARD -> {
                // TODO: remove check after December 15, 2025 and always return Standard.CLASSROOMS
                val now = clock.instant().toEpochMilli()
                if (now in OpenAiAccessService.hourOfAIRange) TeacherSubscriptionLimits.Pro.CLASSROOMS
                else TeacherSubscriptionLimits.Standard.CLASSROOMS
            }

            TeacherSubscriptionPlan.PRO -> TeacherSubscriptionLimits.Pro.CLASSROOMS
        }
        return currentClassCount < maxClassrooms
    }

    override fun studentLimitPerClassExceeded(teacherProfile: TeacherProfile, studentCountInClassroom: Long): Boolean {
        val maxStudents = when (teacherProfile.subscription.plan) {
            TeacherSubscriptionPlan.STANDARD -> {
                // TODO: remove check after December 15, 2025 and always return Standard.STUDENTS_PER_CLASSROOM
                val now = clock.instant().toEpochMilli()
                if (now in OpenAiAccessService.hourOfAIRange) TeacherSubscriptionLimits.Pro.STUDENTS_PER_CLASSROOM
                else TeacherSubscriptionLimits.Standard.STUDENTS_PER_CLASSROOM
            }

            TeacherSubscriptionPlan.PRO -> TeacherSubscriptionLimits.Pro.STUDENTS_PER_CLASSROOM
        }
        return studentCountInClassroom > maxStudents
    }

    override fun cancelSubscription(teacherId: UUID) {
        teacherProfileEntityRepository.findByIdOrNull(teacherId)
            ?.let {
                it.subscriptionCanceled = true
                teacherProfileEntityRepository.save(it)
            }
    }

    override fun hasProSubscription(teacherId: UUID): Boolean {
        return teacherProfileEntityRepository.findByIdOrNull(teacherId)
            ?.hasProSubscription(clock.millis()) ?: false
    }
}
