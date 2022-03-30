package com.anahoret.imagilabsapi.admins.storage

import org.springframework.data.repository.CrudRepository
import java.util.*

interface AdminProfileEntityRepository : CrudRepository<AdminProfileEntity, UUID> {

    fun findByEmail(email: String): AdminProfileEntity?
}
