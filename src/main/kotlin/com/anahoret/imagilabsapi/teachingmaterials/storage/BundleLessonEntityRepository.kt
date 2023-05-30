package com.anahoret.imagilabsapi.teachingmaterials.storage

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

interface BundleLessonEntityRepository : CrudRepository<BundleLessonEntity, UUID> {

    fun deleteAllByBundleId(bundleId: UUID)
    fun findAllByBundleIdOrderByIndex(bundleId: UUID): List<BundleLessonEntity>

    @Query("""
        SELECT bl
        FROM BundleLessonEntity bl
        WHERE bl.bundleId = :bundleId AND (:includePro = true OR bl.proLesson = false)
        ORDER BY bl.index
    """)
    fun findLessonsByIncludedProOrderedByIndex(bundleId: UUID, includePro: Boolean): List<BundleLessonEntity>

    @Query("""
        SELECT bl
        FROM BundleLessonEntity bl
        WHERE bl.bundleId in :bundlesIds
    """)
    fun findAllByBundleIds(bundlesIds: List<UUID>): List<BundleLessonEntity>

    @Query("""
        SELECT bl
        FROM BundleLessonEntity bl
        JOIN TeacherBundleEntity tb ON bl.bundleId = tb.bundleId
        WHERE tb.teacherId = :teacherId AND (:includePro = true OR bl.proLesson = false)
    """)
    fun findAllBundleLessonsByTeacherId(teacherId: UUID, includePro: Boolean): List<BundleLessonEntity>
}
