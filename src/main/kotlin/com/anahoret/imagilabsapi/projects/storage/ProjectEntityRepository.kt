package com.anahoret.imagilabsapi.projects.storage;

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

interface ProjectEntityRepository : CrudRepository<ProjectEntity, UUID> {

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
    fun countByOwnerIds(ownerIds: Iterable<UUID>): Iterable<OwnerProjectCount>

    fun findAllByOwnerId(ownerId: UUID, pageable: Pageable): Page<ProjectEntity>
    fun findAllByIdIn(ids: Collection<UUID>, pageable: Pageable): Page<ProjectEntity>
}

interface OwnerProjectCount {

    val ownerId: UUID
    val projectsCount: Long
}
