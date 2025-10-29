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
        a.username as username,
        a.email as email,
        a.password as password
        FROM LovableAccountEntity a
        WHERE a.connectedUser = :id AND a.active = true
    """
    )
    fun findOneByConnectedUserAndActiveTrue(id: UUID): LovableAccountEntity?

    @Query(
        """SELECT
        a.connectedUser as connectedUser,
        a.username as username,
        a.email as email,
        a.password as password
        FROM LovableAccountEntity a
        WHERE a.connectedUser IN :userIds AND a.active = true
    """
    )
    fun findByConnectedUserIn(userIds: List<UUID>): List<LovableAccountEntity>

    fun deleteByConnectedUserIn(userIds: List<UUID>)
}
