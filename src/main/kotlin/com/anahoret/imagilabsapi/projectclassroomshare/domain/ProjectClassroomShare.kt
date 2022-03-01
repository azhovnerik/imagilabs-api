package com.anahoret.imagilabsapi.projectclassroomshare.domain

import com.anahoret.imagilabsapi.projectclassroomshare.storage.ProjectClassroomShareEntity
import java.util.*

class ProjectClassroomShare(
    val projectId: UUID,
    val classroomId: UUID
) {

    companion object {

        fun fromEntity(projectClassroomShareEntity: ProjectClassroomShareEntity): ProjectClassroomShare {
            return with(projectClassroomShareEntity) {
                ProjectClassroomShare(projectId, classroomId)
            }
        }
    }
}
