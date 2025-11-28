package com.anahoret.imagilabsapi.students.domain

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.students.storage.StudentProfileEntity
import com.anahoret.imagilabsapi.users.UserType
import java.util.*

@Suppress("unused")
class StudentProfile(
    override val id: UUID,
    val name: String,
    val username: String,
    val createdAt: Long,
    val edLinkIntegrationId: UUID? = null,
    val edLinkPersonId: UUID? = null
) : UserProfile {

    override val userType = UserType.STUDENT
    override val fullName = name
    val isEdLinkConnected = edLinkIntegrationId != null && edLinkPersonId != null

    companion object {

        fun fromEntity(studentProfileEntity: StudentProfileEntity): StudentProfile {
            return with(studentProfileEntity) {
                StudentProfile(
                    id!!,
                    name,
                    username,
                    createdAt ?: 0,
                    edLinkIntegrationId = edLinkIntegrationId,
                    edLinkPersonId = edLinkPersonId
                )
            }
        }
    }
}
