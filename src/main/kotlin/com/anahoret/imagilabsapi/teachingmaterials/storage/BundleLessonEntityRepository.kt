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
        WHERE bl.bundleId = :bundleId
        ORDER BY bl.index
    """)
    fun findLessonsByBundleIdOrderedByIndex(bundleId: UUID): List<BundleLessonEntity>

    @Query("""
        SELECT bl
        FROM BundleLessonEntity bl
        WHERE bl.bundleId in :bundlesIds
    """)
    fun findAllByBundleLessonsIds(bundlesIds: List<UUID>): List<BundleLessonEntity>

    @Query("""
        SELECT bl
        FROM BundleLessonEntity bl
        JOIN TeacherBundleEntity tb ON bl.bundleId = tb.bundleId
        WHERE tb.teacherId = :teacherId
    """)
    fun findAllBundleLessonsByTeacherId(teacherId: UUID): List<BundleLessonEntity>
}
