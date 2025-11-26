package com.anahoret.imagilabsapi.schools.storage

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SchoolRepository : JpaRepository<SchoolEntity, UUID> {
    fun findByNameIgnoreCase(name: String): SchoolEntity?
    fun existsByNameIgnoreCase(name: String): Boolean
}