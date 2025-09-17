package com.anahoret.imagilabsapi.lovable.storage

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface LovableAccountRepository : JpaRepository<LovableAccountEntity, UUID> {
    fun findOneByConnectedUserIsNull(): LovableAccountEntity?
    fun findByConnectedUser(id: UUID): List<LovableAccountEntity>
    fun countByConnectedUser(id: UUID): Long
}
