package com.anahoret.imagilabsapi.projects.domain

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.projectclassroomshare.domain.ProjectClassroomShareDetails
import com.anahoret.imagilabsapi.pythoncompiler.domain.RunCodeResponse
import com.anahoret.imagilabsapi.users.UserType
import java.util.*

@Suppress("unused")
class ProjectDetails(
    val id: UUID,
    val name: String,
    val ownerId: UUID,
    val ownerUserType: UserType,
    val ownerName: String,
    val sourceCode: String,
    val runCodeResponse: RunCodeResponse?,
    val canEdit: Boolean,
    val canUnshare: Boolean,
    val classroomShares: List<ProjectClassroomShareDetails>,
    val lastModifiedAt: Long,
    val createdAt: Long
) {

    val shared: Boolean = classroomShares.isNotEmpty()

    companion object {

        fun fromProject(
            project: Project,
            owner: UserProfile,
            canEdit: Boolean,
            canUnshare: Boolean,
            classroomShares: List<ProjectClassroomShareDetails>
        ): ProjectDetails {
            return with(project) {
                ProjectDetails(
                    id = id,
                    name = name,
                    ownerId = ownerId,
                    ownerUserType = ownerUserType,
                    ownerName = owner.fullName,
                    sourceCode = sourceCode,
                    runCodeResponse = runCodeResponse,
                    canEdit = canEdit,
                    canUnshare = canUnshare,
                    classroomShares = classroomShares,
                    lastModifiedAt = lastModifiedAt,
                    createdAt = createdAt
                )
            }
        }
    }
}
