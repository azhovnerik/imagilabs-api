package com.anahoret.imagilabsapi.projects.storage

import com.anahoret.imagilabsapi.projects.domain.ProjectState
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

interface ProjectEntityRepository : CrudRepository<ProjectEntity, UUID>, ProjectEntityRepositoryCustom {

    @Query(
        """
        SELECT 
            p.ownerId AS ownerId,
            COUNT(p.id) AS projectsCount                
        FROM ProjectEntity p
        WHERE p.ownerId IN :ownerIds
        GROUP BY p.ownerId
        """,
    )
    fun countByOwnerIds(ownerIds: Collection<UUID>): Iterable<OwnerProjectCount>

    fun findAllByOwnerId(ownerId: UUID, pageable: Pageable): Page<ProjectEntity>
    fun findAllByIdIn(ids: Collection<UUID>, pageable: Pageable): Page<ProjectEntity>
    fun findAllByOwnerId(ownerId: UUID): List<ProjectEntity>
    fun deleteAllByOwnerId(ownerId: UUID)
    fun findAllByOwnerIdIn(ownerIds: Collection<UUID>): List<ProjectEntity>

    @Query(
        """
        SELECT DISTINCT p FROM ProjectEntity p
        JOIN ProjectClassroomShareEntity pcs ON pcs.projectId = p.id
        WHERE p.ownerId = :ownerId AND pcs.classroomId IN :sharedInClassesIds
    """
    )
    fun findSharedInClasses(
        ownerId: UUID,
        sharedInClassesIds: Collection<UUID>,
        pageable: Pageable
    ): Page<ProjectEntity>

    @Query(
        """
        SELECT DISTINCT p FROM ProjectEntity p
        JOIN ProjectClassroomShareEntity pcs ON pcs.projectId = p.id
        WHERE p.ownerId = :ownerId
    """
    )
    fun findShared(
        ownerId: UUID,
        pageable: Pageable
    ): Page<ProjectEntity>

    @Query(
        """
        SELECT DISTINCT p FROM ProjectEntity p
        LEFT JOIN ProjectClassroomShareEntity pcs ON pcs.projectId = p.id
        WHERE p.ownerId = :ownerId AND pcs IS NULL
    """
    )
    fun findDraft(
        ownerId: UUID,
        pageable: Pageable
    ): Page<ProjectEntity>
}

interface ProjectEntityRepositoryCustom {

    fun search(
        ownerId: UUID,
        state: ProjectState?,
        sharedInClassesIds: Set<UUID>?,
        pageable: Pageable
    ): Page<ProjectEntity>
}

@Repository
class ProjectEntityRepositoryCustomImpl(
    @Lazy private val projectEntityRepository: ProjectEntityRepository
) : ProjectEntityRepositoryCustom {

    override fun search(
        ownerId: UUID,
        state: ProjectState?,
        sharedInClassesIds: Set<UUID>?,
        pageable: Pageable
    ): Page<ProjectEntity> {
        return when {
            sharedInClassesIds != null && sharedInClassesIds.isNotEmpty() && state == ProjectState.DRAFT -> Page.empty()
            sharedInClassesIds != null && sharedInClassesIds.isNotEmpty() ->
                projectEntityRepository.findSharedInClasses(ownerId, sharedInClassesIds, pageable)
            state == ProjectState.SHARED -> projectEntityRepository.findShared(ownerId, pageable)
            state == ProjectState.DRAFT -> projectEntityRepository.findDraft(ownerId, pageable)
            else -> projectEntityRepository.findAllByOwnerId(ownerId, pageable)
        }
    }

}

interface OwnerProjectCount {

    val ownerId: UUID
    val projectsCount: Long
}
