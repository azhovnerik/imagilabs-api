package com.anahoret.imagilabsapi.teachingmaterials.storage

import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.*

interface LessonBundleEntityRepository : JpaRepository<LessonBundleEntity, UUID> {

    fun findAllByNameContainingIgnoreCase(searchQuery: String, sort: Sort): List<LessonBundleEntity>
    fun findByDefaultBundleTrue(): LessonBundleEntity?
}
