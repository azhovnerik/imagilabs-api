package com.anahoret.imagilabsapi.projects.storage;

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
}

interface OwnerProjectCount {

    val ownerId: UUID
    val projectsCount: Long
}
