package com.anahoret.imagilabsapi.teachers.domain

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntity
import com.anahoret.imagilabsapi.users.UserType
import java.util.*

class TeacherProfile(
    override val id: UUID,
    val firstName: String,
    val lastName: String,
    val emailVerified: Boolean
) : UserProfile {

    override val userType = UserType.TEACHER

    companion object {

        fun fromEntity(entity: TeacherProfileEntity): TeacherProfile {
            return with(entity) {
                TeacherProfile(id!!, firstName, lastName, emailVerified)
            }
        }
    }
}
