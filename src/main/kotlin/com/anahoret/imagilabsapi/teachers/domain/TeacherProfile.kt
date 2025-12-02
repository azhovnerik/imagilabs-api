package com.anahoret.imagilabsapi.teachers.domain

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscription
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionData
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntity
import com.anahoret.imagilabsapi.users.UserType
import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*
import kotlin.String

@Suppress("unused")
class TeacherProfile(
    override val id: UUID,
    val firstName: String,
    val lastName: String,
    var email: String,
    val country: String,
    val organization: String,
    val howDidYouHearAboutUs: String,
    val howDidYouHearAboutUsOther: String?,
    val createdAt: Long,
    val emailVerified: Boolean,
    val marketingEmailSubscribed: Boolean,
    val subscription: TeacherSubscription,
    val state: String? = null,
    val schoolRoles: List<SchoolRole> = emptyList(),
    val grades: List<GradeLevel> = emptyList(),
    val subjects: String? = null,
    val schools: String? = null,
    @field:JsonIgnore val edLinkIntegrationId: UUID? = null,
    @field:JsonIgnore val edLinkPersonId: UUID? = null,
) : UserProfile {

    override val userType = UserType.TEACHER
    override val fullName = "$firstName $lastName"
    val isEdLinkConnected = edLinkIntegrationId != null && edLinkPersonId != null

    fun hasProSubscription(now: Long): Boolean = subscription.hasProSubscription(now)

    companion object {

        fun fromEntity(entity: TeacherProfileEntity, subscription: TeacherSubscription): TeacherProfile {
            return with(entity) {
                TeacherProfile(
                    id!!,
                    firstName,
                    lastName,
                    email,
                    country,
                    organization,
                    howDidYouHearAboutUs,
                    howDidYouHearAboutUsOther,
                    createdAt ?: 0,
                    emailVerified,
                    marketingEmailSubscribed,
                    subscription,
                    state = state,
                    schoolRoles = schoolRoles,
                    grades = grades,
                    subjects = subjects,
                    schools = schools,
                    edLinkIntegrationId = edLinkIntegrationId,
                    edLinkPersonId = edLinkPersonId
                )
            }
        }
    }
}
