package com.anahoret.imagilabsapi.lovable.domain

import java.util.*

class LovableAccount(
    val connectedUserId: UUID?,
    val connectedStudentName: String?,
    val username: String?,
    val email: String,
    val password: String
)
