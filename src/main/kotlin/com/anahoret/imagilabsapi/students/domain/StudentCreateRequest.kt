package com.anahoret.imagilabsapi.students.domain

import java.util.*

class StudentCreateRequest(
    val name: String,
    val edLinkIntegrationId: UUID? = null,
    val edLinkPersonId: UUID? = null
)
