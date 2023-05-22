package com.anahoret.imagilabsapi.subscription.domain

import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntityRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Clock
import java.util.*

interface TeacherSubscriptionService {
    fun setPeriod(teacherId: UUID, start: Long, end: Long)
    fun buildSubscriptionDto(teacherSubscriptionData: TeacherSubscriptionData): TeacherSubscription
    fun cancelSubscription(teacherId: UUID)
}

@Service
class TeacherSubscriptionServiceImpl(
    private val teacherProfileEntityRepository: TeacherProfileEntityRepository,
    private val clock: Clock
) : TeacherSubscriptionService {
    override fun setPeriod(teacherId: UUID, start: Long, end: Long) {
        teacherProfileEntityRepository.findByIdOrNull(teacherId)
            ?.let {
                it.subscriptionStart = start
                it.subscriptionEnd = end
                teacherProfileEntityRepository.save(it)
            }
    }

    override fun buildSubscriptionDto(teacherSubscriptionData: TeacherSubscriptionData): TeacherSubscription {
        with(teacherSubscriptionData) {
            val now = clock.instant().toEpochMilli()
            return if (subscriptionStart == null || subscriptionEnd == null || now > subscriptionEnd!! || now < subscriptionStart!!) {
                TeacherSubscription(subscriptionStart, subscriptionEnd, TeacherSubscriptionPlan.STANDARD, subscriptionCanceled)
            } else {
                TeacherSubscription(subscriptionStart, subscriptionEnd, TeacherSubscriptionPlan.PRO, subscriptionCanceled)
            }
        }
    }

    override fun cancelSubscription(teacherId: UUID) {
        teacherProfileEntityRepository.findByIdOrNull(teacherId)
            ?.let {
                it.subscriptionCanceled = true
                teacherProfileEntityRepository.save(it)
            }
    }
}
