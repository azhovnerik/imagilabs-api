package com.anahoret.imagilabsapi.projects.domain

import com.anahoret.imagilabsapi.pythoncompiler.domain.RunCodeResponse
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
    val canUnshare: Boolean,
    val shared: Boolean,
    val lastModifiedAt: Long,
    val createdAt: Long
) {

    companion object {

        fun fromProject(
            project: Project,
            canEdit: Boolean,
            canUnshare: Boolean,
            shared: Boolean
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
                    canUnshare = canUnshare,
                    shared = shared,
                    lastModifiedAt = lastModifiedAt,
                    createdAt = createdAt
                )
            }
        }
    }
}
