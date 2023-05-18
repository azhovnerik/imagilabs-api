package com.anahoret.imagilabsapi.subscription.domain

import org.springframework.stereotype.Service
import java.util.*

interface TeacherSubscriptionService {
    fun setPeriod(teacherId: UUID, start: Long, end: Long)
}

@Service
class TeacherSubscriptionServiceImpl : TeacherSubscriptionService {
    override fun setPeriod(teacherId: UUID, start: Long, end: Long) {
        TODO("Not yet implemented")
    }

}
