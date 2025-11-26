package com.anahoret.imagilabsapi.teachers.domain

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscription
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionData
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntity
import com.anahoret.imagilabsapi.users.UserType
import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*

@Suppress("unused")
class TeacherProfile(
    override val id: UUID,
    val firstName: String,
    val lastName: String,
    var email: String,
    val country: String,
    val organization: String,
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
    @field:JsonIgnore val edLinkPersonId: UUID? = null
) : UserProfile, TeacherSubscriptionData by subscription {

    override val userType = UserType.TEACHER
    override val fullName = "$firstName $lastName"

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
                    createdAt ?: 0,
                    emailVerified,
                    marketingEmailSubscribed,
                    subscription,
                    state = state,
                    schoolRoles = parseSchoolRoles(schoolRoles),
                    grades = parseGrades(grades),
                    subjects = subjects,
                    schools = schools,
                    edLinkIntegrationId = edLinkIntegrationId,
                    edLinkPersonId = edLinkPersonId
                )
            }
        }

        fun parseSchoolRoles(rolesString: String?): List<SchoolRole> {
            return rolesString?.split(",")
                ?.mapNotNull {
                    try {
                        SchoolRole.valueOf(it.trim())
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                } ?: emptyList()
        }

        fun parseGrades(gradesString: String?): List<GradeLevel> {
            return gradesString?.split(",")
                ?.mapNotNull {
                    try {
                        GradeLevel.valueOf(it.trim())
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                } ?: emptyList()
        }

        fun schoolRolesToString(roles: List<SchoolRole>?): String? {
            return roles?.takeIf { it.isNotEmpty() }?.joinToString(",") { it.name }
        }

        fun gradesToString(grades: List<GradeLevel>?): String? {
            return grades?.takeIf { it.isNotEmpty() }?.joinToString(",") { it.name }
        }
    }
}
