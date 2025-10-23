package com.anahoret.imagilabsapi.edlink.storage

import org.springframework.data.repository.CrudRepository
import java.util.*

interface EdLinkOAuthStateRepository : CrudRepository<EdLinkOAuthStateEntity, UUID> {

    fun deleteByCreatedAtLessThan(timestamp: Long): Int

}
