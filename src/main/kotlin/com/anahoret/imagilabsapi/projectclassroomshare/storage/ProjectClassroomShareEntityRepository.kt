package com.anahoret.imagilabsapi.projectclassroomshare.storage

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

interface ProjectClassroomShareEntityRepository : CrudRepository<ProjectClassroomShareEntity, UUID> {

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
    fun getProjectCountsByClassrooms(classroomIds: Iterable<UUID>): Iterable<ClassroomSharedProjectCount>
    fun countByClassroomId(classroomId: UUID): Long
    fun findAllByProjectId(projectId: UUID): Iterable<ProjectClassroomShareEntity>
    fun findAllByClassroomId(classroomId: UUID): Iterable<ProjectClassroomShareEntity>

    @Query(
        """
        SELECT
            p.ownerId AS ownerId,
            COUNT(p.id) AS projectsCount
        FROM ProjectClassroomShareEntity pcs
        JOIN ProjectEntity p ON pcs.projectId = p.id
        WHERE p.ownerId IN :ownerIds
        GROUP BY p.ownerId
        """
    )
    fun getProjectCountsByOwners(ownerIds: Iterable<UUID>): Iterable<OwnerSharedProjectCount>

    @Query(
        """
        SELECT pcs
        FROM ProjectClassroomShareEntity pcs
        JOIN ProjectEntity p ON pcs.projectId = p.id
        WHERE p.ownerId = :ownerId
    """
    )
    fun findAllByOwnerId(ownerId: UUID): Iterable<ProjectClassroomShareEntity>
}

interface ClassroomSharedProjectCount {

    val classroomId: UUID
    val projectsCount: Long
}

interface OwnerSharedProjectCount {

    val ownerId: UUID
    val projectsCount: Long
}
