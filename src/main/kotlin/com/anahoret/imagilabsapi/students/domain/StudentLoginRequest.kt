package com.anahoret.imagilabsapi.students.domain

class StudentLoginRequest(
    val username: String,
    val classroomAccessCode: String,
    val password: String,
    val mobileAppClient: Boolean
)
