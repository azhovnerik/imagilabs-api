package com.anahoret.imagilabsapi.teachers.domain

class TeacherSignupRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val country: String,
    val organization: String,
    val howDidYouHearAboutUs: String
)
