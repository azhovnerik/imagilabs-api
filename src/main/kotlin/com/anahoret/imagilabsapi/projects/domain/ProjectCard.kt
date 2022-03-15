package com.anahoret.imagilabsapi.projects.domain

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.pythoncompiler.RunCodeResponse
import com.anahoret.imagilabsapi.users.UserType
import java.util.*

class ProjectCard(
    val id: UUID,
    val name: String,
    val ownerId: UUID,
    val ownerUserType: UserType,
    val ownerName: String,
    val runCodeResponse: RunCodeResponse?,
    val lastModifiedAt: Long,
    val createdAt: Long
) {

    companion object {

        fun fromProject(project: Project, owner: UserProfile): ProjectCard {
            return with(project) {
                ProjectCard(
                    id,
                    name,
                    ownerId,
                    ownerUserType,
                    owner.fullName,
                    runCodeResponse,
                    lastModifiedAt,
                    createdAt
                )
            }
        }
    }
}
