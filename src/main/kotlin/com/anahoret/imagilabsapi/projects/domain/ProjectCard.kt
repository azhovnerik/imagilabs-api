package com.anahoret.imagilabsapi.projects.domain

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.pythoncompiler.domain.RunCodeResponse
import com.anahoret.imagilabsapi.users.UserType
import java.util.*

@Suppress("unused")
class ProjectCard(
    val id: UUID,
    val name: String,
    val ownerId: UUID,
    val ownerUserType: UserType,
    val ownerName: String,
    val runCodeResponse: RunCodeResponse?,
    val shared: Boolean,
    val lastModifiedAt: Long,
    val createdAt: Long
) {

    companion object {

        fun fromProject(
            project: Project,
            owner: UserProfile,
            shared: Boolean
        ): ProjectCard {
            return with(project) {
                ProjectCard(
                    id = id,
                    name = name,
                    ownerId = ownerId,
                    ownerUserType = ownerUserType,
                    ownerName = owner.fullName,
                    runCodeResponse = runCodeResponse,
                    shared = shared,
                    lastModifiedAt = lastModifiedAt,
                    createdAt = createdAt
                )
            }
        }
    }
}
