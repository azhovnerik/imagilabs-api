package com.anahoret.imagilabsapi.lovable.storage

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface LovableAccountRepository : JpaRepository<LovableAccountEntity, UUID> {
    fun findFirstByConnectedUserIsNull(): LovableAccountEntity?
    fun findByConnectedUser(id: UUID): List<LovableAccountEntity>
    fun countByConnectedUser(id: UUID): Long

    @Query(
        """SELECT
        a.connectedUser as connectedUser,
        sp.username as username,
        a.email as email,
        a.password as password
        FROM LovableAccountEntity a
        LEFT JOIN StudentProfileEntity sp ON sp.id = :id
        WHERE a.connectedUser = :id AND a.active = true
    """
    )
    fun findOneByConnectedUserAndActiveTrue(id: UUID): LovableAccountProjection?

    @Query(
        """SELECT
        a.connectedUser as connectedUser,
        sp.username as username,
        a.email as email,
        a.password as password
        FROM LovableAccountEntity a
        LEFT JOIN StudentProfileEntity sp ON sp.id = :id
        WHERE a.connectedUser IN :userIds AND a.active = true
    """
    )
    fun findByConnectedUserIn(userIds: List<UUID>): List<LovableAccountProjection>

    fun deleteByConnectedUserIn(userIds: List<UUID>)
}

interface LovableAccountProjection {
    val connectedUser: UUID?
    val username: String?
    val email: String
    val password: String
}
