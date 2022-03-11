package com.anahoret.imagilabsapi.projects.domain

import com.anahoret.imagilabsapi.projects.storage.ProjectEntity
import com.anahoret.imagilabsapi.pythoncompiler.RunCodeResponse
import com.anahoret.imagilabsapi.users.UserType
import java.util.*

class Project(
    val id: UUID,
    val name: String,
    val ownerId: UUID,
    val ownerUserType: UserType,
    val sourceCode: String,
    val runCodeResponse: RunCodeResponse?,
    val lastModifiedAt: Long,
    val createdAt: Long
) {

    companion object {

        fun fromEntity(projectEntity: ProjectEntity, parseToRunCodeResponse: (String) -> RunCodeResponse): Project {
            return with(projectEntity) {
                val runCodeResponse = runResult?.let(parseToRunCodeResponse)
                Project(
                    id!!, name, ownerId, ownerUserType, sourceCode, runCodeResponse, lastModifiedAt!!, createdAt!!
                )
            }
        }
    }
}
