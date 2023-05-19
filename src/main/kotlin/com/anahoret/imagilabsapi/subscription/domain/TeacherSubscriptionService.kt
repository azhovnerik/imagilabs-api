package com.anahoret.imagilabsapi.subscription.domain

import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntityRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

interface TeacherSubscriptionService {
    fun setPeriod(teacherId: UUID, start: Long, end: Long)
}

@Service
class TeacherSubscriptionServiceImpl(
    private val teacherProfileEntityRepository: TeacherProfileEntityRepository
) : TeacherSubscriptionService {
    override fun setPeriod(teacherId: UUID, start: Long, end: Long) {
        teacherProfileEntityRepository.findByIdOrNull(teacherId)
            ?.let {
                it.subscriptionStart = start
                it.subscriptionEnd = end
                teacherProfileEntityRepository.save(it)
            }
    }

}
