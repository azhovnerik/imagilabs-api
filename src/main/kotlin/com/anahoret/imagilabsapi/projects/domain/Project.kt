package com.anahoret.imagilabsapi.projects.domain

import com.anahoret.imagilabsapi.projects.storage.ProjectEntity
import com.anahoret.imagilabsapi.users.UserType
import java.util.*

class Project(
    val id: UUID,
    val name: String,
    val ownerId: UUID,
    val ownerUserType: UserType,
    val sourceCode: String,
    val runResult: String?,
    val lastModifiedAt: Long,
    val createdAt: Long
) {

    companion object {

        fun fromEntity(projectEntity: ProjectEntity): Project {
            return with(projectEntity) {
                Project(
                    id!!, name, ownerId, ownerUserType, sourceCode, runResult, lastModifiedAt!!, createdAt!!
                )
            }
        }
    }
}
