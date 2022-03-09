package com.anahoret.imagilabsapi.teachingmaterials.storage;

import org.springframework.data.repository.CrudRepository
import java.util.*

interface TeachingMaterialEntityRepository : CrudRepository<TeachingMaterialEntity, UUID> {

    fun findAllByOrderByIndex(): List<TeachingMaterialEntity>
}
