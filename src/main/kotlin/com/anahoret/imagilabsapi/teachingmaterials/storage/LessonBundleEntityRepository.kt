package com.anahoret.imagilabsapi.teachingmaterials.storage

import org.springframework.data.domain.Sort
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.*

interface LessonBundleEntityRepository : PagingAndSortingRepository<LessonBundleEntity, UUID> {

    fun findAllByNameContainingIgnoreCase(searchQuery: String, sort: Sort): List<LessonBundleEntity>
    fun findByDefaultBundleTrue(): LessonBundleEntity?
}
