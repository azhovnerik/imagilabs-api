package com.anahoret.imagilabsapi.teachers.domain

import java.util.*

class TeacherSignupRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val country: String,
    val organization: String,
    val howDidYouHearAboutUs: String,
    val howDidYouHearAboutUsOther: String?,
    val marketingEmailSubscribed: Boolean,
    val mobileAppClient: Boolean,
    val edLinkIntegrationId: UUID? = null,
    val edLinkPersonId: UUID? = null,
    val state: String? = null,
    val schoolRoles: List<SchoolRole> = emptyList(),
    val grades: List<GradeLevel> = emptyList(),
    val subjects: String? = null,
    val schools: String? = null,
) {

    fun normalize(): TeacherSignupRequest {
        return TeacherSignupRequest(
            email.trim().lowercase(),
            password.trim(),
            firstName.trim(),
            lastName.trim(),
            country.trim(),
            organization.trim(),
            howDidYouHearAboutUs.trim(),
            howDidYouHearAboutUsOther?.trim(),
            marketingEmailSubscribed,
            mobileAppClient,
            edLinkIntegrationId,
            edLinkPersonId
        )
    }

    fun isEdLinkSignUp(): Boolean = edLinkIntegrationId != null && edLinkPersonId != null

}
