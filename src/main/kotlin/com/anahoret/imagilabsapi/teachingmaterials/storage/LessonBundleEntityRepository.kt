package com.anahoret.imagilabsapi.teachingmaterials.storage

import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface LessonBundleEntityRepository : JpaRepository<LessonBundleEntity, UUID> {

    fun findAllByNameContainingIgnoreCase(searchQuery: String, sort: Sort): List<LessonBundleEntity>
    fun findByDefaultBundleTrue(): LessonBundleEntity?

    @Query("""
        SELECT lb
        FROM LessonBundleEntity lb
        JOIN TeacherBundleEntity tb ON lb.id = tb.bundleId
        WHERE tb.teacherId = :teacherId
    """)
    fun findAllByTeacherId(teacherId: UUID): List<LessonBundleEntity>
}
