package com.anahoret.imagilabsapi.projectclassroomshare.storage

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

interface ProjectClassroomShareEntityRepository : CrudRepository<ProjectClassroomShareEntity, UUID> {

    fun existsByProjectIdAndClassroomId(projectId: UUID, classroomId: UUID): Boolean

    @Query(
        """
        SELECT 
            CAST (pcs.classroom_id AS TEXT) AS classroomid,
            COUNT(pcs.project_id) AS projectscount                
        FROM project_classroom_share pcs
        WHERE pcs.classroom_id IN :classroomIds
        GROUP BY pcs.classroom_id
        """,
        nativeQuery = true
    )
    fun getProjectCounts(classroomIds: Iterable<UUID>): Iterable<ClassroomProjectCount>
    fun countByClassroomId(classroomId: UUID): Long
    fun findAllByProjectId(projectId: UUID): Iterable<ProjectClassroomShareEntity>
    fun findAllByClassroomId(classroomId: UUID): Iterable<ProjectClassroomShareEntity>
}

interface ClassroomProjectCount {

    val classroomId: UUID
    val projectsCount: Long
}
