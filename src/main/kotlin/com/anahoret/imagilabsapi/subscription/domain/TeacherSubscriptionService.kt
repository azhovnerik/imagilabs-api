package com.anahoret.imagilabsapi.subscription.domain

import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntityRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Clock
import java.util.*

interface TeacherSubscriptionService {
    fun setPeriod(teacherId: UUID, start: Long, end: Long)
    fun buildSubscriptionDto(start: Long?, end: Long?): TeacherSubscription
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

    override fun buildSubscriptionDto(start: Long?, end: Long?): TeacherSubscription {
        val now = clock.instant().toEpochMilli()
        return if (start == null || end == null || now > end || now < start) {
            TeacherSubscription(start, end, TeacherSubscriptionPlan.STANDARD)
        } else {
            TeacherSubscription(start, end, TeacherSubscriptionPlan.PRO)
        }
    }


}
