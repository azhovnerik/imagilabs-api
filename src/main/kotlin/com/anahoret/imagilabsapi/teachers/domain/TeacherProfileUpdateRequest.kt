package com.anahoret.imagilabsapi.teachers.domain

import java.util.*

data class TeacherProfileUpdateRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val country: String? = null,
    val state: String? = null,
    val organization: String? = null,
    val schoolIds: List<UUID>? = null,
    val grades: List<GradeLevel>? = null,
    val schoolRoles: List<SchoolRole>? = null,
    val subjects: List<Subject>? = null,
    val marketingEmailSubscribed: Boolean? = null
)
