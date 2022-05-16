package com.anahoret.imagilabsapi.projectclassroomshare.storage

import org.springframework.context.annotation.Lazy
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

interface ProjectClassroomShareEntityRepository : CrudRepository<ProjectClassroomShareEntity, UUID>,
    ProjectClassroomShareEntityRepositoryCustom {

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

    fun deleteAllByProjectIdAndClassroomIdIn(projectId: UUID, classroomIds: List<UUID>)
    fun deleteAllByProjectId(projectId: UUID)
    fun deleteAllByProjectIdIn(projectIds: Collection<UUID>)
    fun deleteAllByClassroomId(classroomId: UUID)

    @Query(
        """
        SELECT pcs FROM ProjectClassroomShareEntity pcs
        JOIN ProjectEntity p ON p.id = pcs.projectId
        WHERE pcs.classroomId = :classroomId AND p.ownerId = :ownerId
    """
    )
    fun findAllByClassroomIdAndOwnerId(classroomId: UUID, ownerId: UUID): Iterable<ProjectClassroomShareEntity>

    @Query(
        """
            SELECT pcs FROM ProjectClassroomShareEntity pcs
            JOIN ProjectEntity p ON p.id = pcs.projectId
            LEFT JOIN StudentProfileEntity sp ON p.ownerUserType = 'STUDENT' AND sp.id = p.ownerId
            LEFT JOIN TeacherProfileEntity tp ON p.ownerUserType = 'TEACHER' AND tp.id = p.ownerId
            WHERE pcs.classroomId = :classroomId AND (
                LOWER(p.name) LIKE CONCAT('%', :searchQuery, '%') OR
                LOWER(sp.name) LIKE CONCAT('%', :searchQuery, '%') OR
                LOWER(CONCAT(tp.firstName, ' ', tp.lastName)) LIKE CONCAT('%', :searchQuery, '%')
            )
        """
    )
    fun findAllByClassroomIdAndSearchQuery(
        classroomId: UUID,
        searchQuery: String
    ): Iterable<ProjectClassroomShareEntity>
}

interface ProjectClassroomShareEntityRepositoryCustom {

    fun search(classroomId: UUID, ownerId: UUID?, searchQuery: String?): Iterable<ProjectClassroomShareEntity>
}

@Suppress("unused")
class ProjectClassroomShareEntityRepositoryCustomImpl(
    @Lazy private val projectClassroomShareEntityRepository: ProjectClassroomShareEntityRepository
) : ProjectClassroomShareEntityRepositoryCustom {

    override fun search(
        classroomId: UUID,
        ownerId: UUID?,
        searchQuery: String?
    ): Iterable<ProjectClassroomShareEntity> {
        return when {
            ownerId != null ->
                projectClassroomShareEntityRepository.findAllByClassroomIdAndOwnerId(
                    classroomId,
                    ownerId
                )

            searchQuery != null && searchQuery.trim().isNotBlank() ->
                projectClassroomShareEntityRepository.findAllByClassroomIdAndSearchQuery(
                    classroomId,
                    searchQuery.lowercase()
                )

            else -> projectClassroomShareEntityRepository.findAllByClassroomId(classroomId)
        }
    }

}


interface ClassroomSharedProjectCount {

    val classroomId: UUID
    val projectsCount: Long
}

interface OwnerSharedProjectCount {

    val ownerId: UUID
    val projectsCount: Long
}
