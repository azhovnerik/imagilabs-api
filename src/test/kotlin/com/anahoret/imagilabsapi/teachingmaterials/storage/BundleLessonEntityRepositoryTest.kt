package com.anahoret.imagilabsapi.teachingmaterials.storage

import com.anahoret.imagilabsapi.spring.ImagiLabsDatabaseTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

@ImagiLabsDatabaseTest
@DisplayName("Bundle lesson entity repository")
class BundleLessonEntityRepositoryTest {

    @Autowired
    lateinit var lessonBundleEntityRepository: LessonBundleEntityRepository

    @Autowired
    lateinit var bundleLessonEntityRepository: BundleLessonEntityRepository

    @Autowired
    lateinit var teacherBundleRepository: TeacherBundleRepository

    lateinit var bundleId: UUID

    private val teacherId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        setupBundle()
        setupBundleLessons()
        setupTeacherBundle()
    }

    private fun setupBundle() {
        bundleId = lessonBundleEntityRepository.save(
            LessonBundleEntity("Introduction in Kotlin", true)
        ).id!!
    }

    private fun setupBundleLessons() {
        bundleLessonEntityRepository.saveAll(listOf(
            bundleLessonEntity(0),
            bundleLessonEntity(1),
            bundleLessonEntity(2),
            bundleLessonEntity(3, true),
            bundleLessonEntity(4, true),
            bundleLessonEntity(5, true)
        ))
    }

    private fun setupTeacherBundle() {
        teacherBundleRepository.save(TeacherBundleEntity(teacherId, bundleId))
    }

    @DisplayName("find lessons by included pro ordered by index")
    @Nested
    inner class FindLessonsByIncludedProOrderedByIndex {

        @Test
        fun `should return lessons without pro lessons`() {
            val result = bundleLessonEntityRepository.findLessonsByBundleIdOrderedByIndex(bundleId, false)
            assertFalse(result.isEmpty())
            assertTrue(result.size == 3)
            assertTrue(result[0].index == 0)
            assertTrue(result[1].index == 1)
            assertTrue(result[2].index == 2)
        }

        @Test
        fun `should return lessons with pro lessons`() {
            val result = bundleLessonEntityRepository.findLessonsByBundleIdOrderedByIndex(bundleId, true)
            assertFalse(result.isEmpty())
            assertTrue(result.size == 6)
            assertTrue(result[0].index == 0)
            assertTrue(result[1].index == 1)
            assertTrue(result[2].index == 2)

            assertTrue(result[3].index == 3)
            assertTrue(result[3].proLesson)

            assertTrue(result[4].index == 4)
            assertTrue(result[4].proLesson)

            assertTrue(result[5].index == 5)
            assertTrue(result[5].proLesson)
        }
    }

    @DisplayName("find all by bundle ids")
    @Nested
    inner class FindAllByBundlesIds {

        @Test
        fun `should return six bundles lessons by bundles ids`() {
            val bundlesIds = listOf(bundleId)
            val result = bundleLessonEntityRepository.findAllByBundleLessonsIds(bundlesIds, true)
            assertFalse(result.isEmpty())
            assertTrue(result.size == 6)
        }

        @Test
        fun `should return no one bundles lessons`() {
            val bundlesIds = emptyList<UUID>()
            val result = bundleLessonEntityRepository.findAllByBundleLessonsIds(bundlesIds, true)
            assertTrue(result.isEmpty())
        }
    }

    @DisplayName("find all bundle lessons by teacher id")
    @Nested
    inner class FindAllBundleLessonsByTeacherId {

        @Test
        fun `should return bundles lessons of teacher without pro lessons`() {
            val result = bundleLessonEntityRepository.findAllBundleLessonsByTeacherId(teacherId, false)
            assertFalse(result.isEmpty())
            assertTrue(result.size == 3)
            assertTrue(result.none { it.proLesson })
        }

        @Test
        fun `should return bundles lessons of teacher with pro lessons`() {
            val result = bundleLessonEntityRepository.findAllBundleLessonsByTeacherId(teacherId, true)
            assertFalse(result.isEmpty())
            assertTrue(result.size == 6)
        }

        @Test
        fun `should return no one bundles lessons`() {
            val result = bundleLessonEntityRepository.findAllBundleLessonsByTeacherId(UUID.randomUUID(), false)
            assertTrue(result.isEmpty())
        }
    }

    private fun bundleLessonEntity(index: Int, proLesson: Boolean = false): BundleLessonEntity {
        return BundleLessonEntity(bundleId, index, proLesson, "Test $index", "", "")
    }
}
