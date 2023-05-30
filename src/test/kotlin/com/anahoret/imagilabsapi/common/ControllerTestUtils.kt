package com.anahoret.imagilabsapi.common

import com.anahoret.imagilabsapi.admins.domain.AdminProfile
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscription
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionPlan
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import java.util.*

fun testAdmin(): AdminProfile {
    return AdminProfile(UUID.randomUUID(), "Admin")
}

fun testTeacher(): TeacherProfile {
    return TeacherProfile(
        id = UUID.randomUUID(),
        firstName = "John",
        lastName = "Snow",
        email = "johnsnow@winterfell.com",
        country = "Westeros",
        organization = "Starks",
        createdAt = 0L,
        emailVerified = true,
        marketingEmailSubscribed = false,
        subscription = TeacherSubscription(null, null, TeacherSubscriptionPlan.STANDARD, false)
    )
}

fun testStudent(classroomId: UUID): StudentProfile {
    return StudentProfile(
        UUID.randomUUID(),
        "Martin",
        "mrtinos",
        createdAt = 0L,
        classroomId
    )
}
