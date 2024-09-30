package com.anahoret.imagilabsapi.subscription.domain

import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntityRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Clock
import java.util.*

interface TeacherSubscriptionService {
    fun setPeriod(teacherId: UUID, start: Long, end: Long)
    fun cancelSubscription(teacherId: UUID)
    fun buildSubscriptionDto(teacherSubscriptionData: TeacherSubscriptionData): TeacherSubscription
    fun canCreateClassroom(teacherProfile: TeacherProfile): Boolean
    fun studentLimitPerClassExceeded(teacherProfile: TeacherProfile, studentCountInClassroom: Long): Boolean
    fun getSubscriptionDto(teacherId: UUID): TeacherSubscription?
    fun hasProSubscription(teacherId: UUID): Boolean
}

@Service
class TeacherSubscriptionServiceImpl(
    private val teacherProfileEntityRepository: TeacherProfileEntityRepository,
    private val clock: Clock,
    private val classroomService: ClassroomService
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

    override fun buildSubscriptionDto(teacherSubscriptionData: TeacherSubscriptionData): TeacherSubscription {
        with(teacherSubscriptionData) {
            val now = clock.instant().toEpochMilli()
            return if (hasProSubscription(now)) {
                TeacherSubscription(subscriptionStart, subscriptionEnd, TeacherSubscriptionPlan.PRO, subscriptionCanceled)
            } else {
                TeacherSubscription(subscriptionStart, subscriptionEnd, TeacherSubscriptionPlan.STANDARD, subscriptionCanceled)
            }
        }
    }

    override fun getSubscriptionDto(teacherId: UUID): TeacherSubscription? {
        return teacherProfileEntityRepository.findByIdOrNull(teacherId)
            ?.let { buildSubscriptionDto(it) }
    }

    override fun canCreateClassroom(teacherProfile: TeacherProfile): Boolean {
        val currentClassCount = classroomService.countByTeacher(teacherProfile.id)
        return when (teacherProfile.subscription.plan) {
            TeacherSubscriptionPlan.STANDARD -> currentClassCount < TeacherSubscriptionLimits.Standard.CLASSROOMS
            TeacherSubscriptionPlan.PRO -> currentClassCount < TeacherSubscriptionLimits.Pro.CLASSROOMS
        }
    }

    override fun studentLimitPerClassExceeded(teacherProfile: TeacherProfile, studentCountInClassroom: Long): Boolean {
        return when (teacherProfile.subscription.plan) {
            TeacherSubscriptionPlan.STANDARD -> studentCountInClassroom > TeacherSubscriptionLimits.Standard.STUDENTS_PER_CLASSROOM
            TeacherSubscriptionPlan.PRO -> studentCountInClassroom > TeacherSubscriptionLimits.Pro.STUDENTS_PER_CLASSROOM
        }
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
