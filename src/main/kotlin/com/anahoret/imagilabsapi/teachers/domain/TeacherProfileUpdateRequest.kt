package com.anahoret.imagilabsapi.teachers.domain

data class TeacherProfileUpdateRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val country: String? = null,
    val state: String? = null,
    val organization: String? = null,
    val howDidYouHearAboutUs: String? = null,
    val howDidYouHearAboutUsOther: String? = null,
    val schools: String? = null,
    val grades: List<GradeLevel>? = null,
    val schoolRoles: List<SchoolRole>? = null,
    val subjects: String? = null,
    val marketingEmailSubscribed: Boolean? = null
)
