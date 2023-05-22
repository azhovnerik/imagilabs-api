package com.anahoret.imagilabsapi.subscription.domain

object TeacherSubscriptionLimits {
    object Standard {
        const val CLASSROOMS = 10
        const val STUDENTS_PER_CLASSROOM = 50
    }

    object Pro {
        const val CLASSROOMS = 20
        const val STUDENTS_PER_CLASSROOM = 200
    }
}
