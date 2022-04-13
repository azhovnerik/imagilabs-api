package com.anahoret.imagilabsapi.projects.storage

import com.anahoret.imagilabsapi.projects.domain.ProjectState
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Sort
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

    fun findAllByOwnerId(ownerId: UUID, sort: Sort): List<ProjectEntity>
    fun findAllByIdIn(ids: Collection<UUID>, sort: Sort): List<ProjectEntity>
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
        sort: Sort
    ): List<ProjectEntity>

    @Query(
        """
        SELECT DISTINCT p FROM ProjectEntity p
        JOIN ProjectClassroomShareEntity pcs ON pcs.projectId = p.id
        WHERE p.ownerId = :ownerId
    """
    )
    fun findShared(
        ownerId: UUID,
        sort: Sort
    ): List<ProjectEntity>

    @Query(
        """
        SELECT DISTINCT p FROM ProjectEntity p
        LEFT JOIN ProjectClassroomShareEntity pcs ON pcs.projectId = p.id
        WHERE p.ownerId = :ownerId AND pcs IS NULL
    """
    )
    fun findDraft(
        ownerId: UUID,
        sort: Sort
    ): List<ProjectEntity>
}

interface ProjectEntityRepositoryCustom {

    fun search(
        ownerId: UUID,
        state: ProjectState?,
        sharedInClassesIds: Set<UUID>?,
        sort: Sort
    ): List<ProjectEntity>
}

@Repository
class ProjectEntityRepositoryCustomImpl(
    @Lazy private val projectEntityRepository: ProjectEntityRepository
) : ProjectEntityRepositoryCustom {

    override fun search(
        ownerId: UUID,
        state: ProjectState?,
        sharedInClassesIds: Set<UUID>?,
        sort: Sort
    ): List<ProjectEntity> {
        return when {
            sharedInClassesIds != null && sharedInClassesIds.isNotEmpty() -> projectEntityRepository.findSharedInClasses(
                ownerId,
                sharedInClassesIds,
                sort
            )
            state == ProjectState.SHARED -> projectEntityRepository.findShared(ownerId, sort)
            state == ProjectState.DRAFT -> projectEntityRepository.findDraft(ownerId, sort)
            else -> projectEntityRepository.findAllByOwnerId(ownerId, sort)
        }
    }

}

interface OwnerProjectCount {

    val ownerId: UUID
    val projectsCount: Long
}
