package com.anahoret.imagilabsapi.teachers.domain

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscription
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionData
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntity
import com.anahoret.imagilabsapi.users.UserType
import java.util.*

@Suppress("MemberVisibilityCanBePrivate", "unused")
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
    val subscription: TeacherSubscription
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
                    subscription
                )
            }
        }
    }
}
