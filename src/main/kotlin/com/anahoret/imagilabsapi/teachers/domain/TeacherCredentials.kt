package com.anahoret.imagilabsapi.teachers.domain

import java.util.*

class TeacherCredentials(
    val id: UUID,
    val email: String,
    val passwordHash: String
)
