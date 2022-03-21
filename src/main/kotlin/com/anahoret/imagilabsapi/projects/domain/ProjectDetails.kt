package com.anahoret.imagilabsapi.projects.domain

import com.anahoret.imagilabsapi.pythoncompiler.RunCodeResponse
import com.anahoret.imagilabsapi.users.UserType
import java.util.*

@Suppress("unused")
class ProjectDetails(
    val id: UUID,
    val name: String,
    val ownerId: UUID,
    val ownerUserType: UserType,
    val sourceCode: String,
    val runCodeResponse: RunCodeResponse?,
    val canEdit: Boolean,
    val lastModifiedAt: Long,
    val createdAt: Long
) {

    companion object {

        fun fromProject(
            project: Project,
            canEdit: Boolean
        ): ProjectDetails {
            return with(project) {
                ProjectDetails(
                    id = id,
                    name = name,
                    ownerId = ownerId,
                    ownerUserType = ownerUserType,
                    sourceCode = sourceCode,
                    runCodeResponse = runCodeResponse,
                    canEdit = canEdit,
                    lastModifiedAt = lastModifiedAt,
                    createdAt = createdAt
                )
            }
        }
    }
}
