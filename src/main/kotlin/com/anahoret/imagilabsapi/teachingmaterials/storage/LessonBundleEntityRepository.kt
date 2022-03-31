package com.anahoret.imagilabsapi.teachingmaterials.storage

import org.springframework.data.repository.CrudRepository
import java.util.*

interface LessonBundleEntityRepository : CrudRepository<LessonBundleEntity, UUID> {

    fun findAllByOrderByLastModifiedAt(): List<LessonBundleEntity>
    fun findByDefaultBundleTrue(): LessonBundleEntity?
}
