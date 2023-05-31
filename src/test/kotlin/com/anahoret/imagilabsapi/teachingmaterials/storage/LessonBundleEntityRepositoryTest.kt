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
@DisplayName("Lesson bundle entity repository")
class LessonBundleEntityRepositoryTest {

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
        setupTeacherBundle()
    }

    private fun setupBundle() {
        bundleId = lessonBundleEntityRepository.save(
            LessonBundleEntity("Introduction in Kotlin", true)
        ).id!!
        setupBundleLessons()
    }

    private fun setupTeacherBundle() {
        teacherBundleRepository.save(TeacherBundleEntity(teacherId, bundleId))
    }

    private fun setupBundleLessons() {
        bundleLessonEntityRepository.saveAll(listOf(
            BundleLessonEntity(bundleId, 0, false, "Introduction in Kotlin", "", ""),
            BundleLessonEntity(bundleId, 1, false, "Basic in Kotlin", "", "")
        ))
    }

    @DisplayName("find all by teacher id")
    @Nested
    inner class FindAllByTeacherIdTest {

        @Test
        fun `should find all by teacher id`() {
            val result = lessonBundleEntityRepository.findAllByTeacherId(teacherId)
            assertFalse(result.isEmpty())
        }

        @Test
        fun `should not found any bundles by teacher id`() {
            val result = lessonBundleEntityRepository.findAllByTeacherId(UUID.randomUUID())
            assertTrue(result.isEmpty())
        }
    }
}
