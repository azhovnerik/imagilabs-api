package com.anahoret.imagilabsapi.projectclassroomshare.storage;

import org.springframework.data.repository.CrudRepository
import java.util.*

interface ProjectClassroomShareEntityRepository : CrudRepository<ProjectClassroomShareEntity, UUID> {

    fun existsByProjectIdAndClassroomId(projectId: UUID, classroomId: UUID): Boolean
}
