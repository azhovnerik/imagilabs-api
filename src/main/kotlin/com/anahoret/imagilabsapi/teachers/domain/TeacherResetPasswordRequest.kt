package com.anahoret.imagilabsapi.teachers.domain

class TeacherResetPasswordRequest(
    val email: String,
    val code: String,
    val newPassword: String
)
