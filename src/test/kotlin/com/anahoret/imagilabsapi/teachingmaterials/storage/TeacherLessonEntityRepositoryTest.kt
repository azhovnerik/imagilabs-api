package com.anahoret.imagilabsapi.teachingmaterials.storage

import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntity
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntityRepository

import com.anahoret.imagilabsapi.spring.ImagiLabsDatabaseTest
import com.anahoret.imagilabsapi.teachingmaterials.domain.LessonData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

@ImagiLabsDatabaseTest
@DisplayName("Teacher Lesson entity repository")
class TeacherLessonEntityRepositoryTest {

    @Autowired
    lateinit var teacherLessonEntityRepository: TeacherLessonEntityRepository

    @Autowired
    lateinit var teacherProfileEntityRepository: TeacherProfileEntityRepository

    private lateinit var teacherProfileEntity: TeacherProfileEntity
    private lateinit var teacherId: UUID
    private lateinit var teacherSavedLessons: List<TeacherLessonEntity>


    @BeforeEach
    fun setup() {
        setupTeacher()
        setupTeacherLessons()
    }

    private fun setupTeacher() {
        teacherProfileEntity = teacherProfileEntityRepository.save(
            TeacherProfileEntity("teacher3@mail.com", "", "Teacher", "Edu", "Sweden", "imagi", "other", true)
        )
        teacherId = teacherProfileEntity.id!!
    }

    private fun setupTeacherLessons() {
        val lessonList: List<LessonData> = listOf(
            LessonData(0, "Lesson 1", "", "", false),
            LessonData(1, "Lesson 2", "", "", false),
            LessonData(2, "Lesson 3", "", "", true)
        )
        val lastExistingIndex = teacherLessonEntityRepository.findAllByTeacherIdOrderByIndex(teacherId)
            .lastOrNull()?.index
        val remapFromIndex = if (lastExistingIndex == null) 0 else lastExistingIndex + 1
        val newLessonsWithRemappedIndices = lessonList.map { it.copy(index = remapFromIndex + it.index) }
        teacherSavedLessons = newLessonsWithRemappedIndices.map {
            TeacherLessonEntity(teacherId, it.index, it.locked, it.name, it.worksheetUri, it.slidesUri)
        }.let(teacherLessonEntityRepository::saveAll) as List<TeacherLessonEntity>
    }

    @DisplayName("when fetching teacher lessons")
    @Nested
    inner class TeacherProfileFetchTest {

        @Test
        fun `should find teacher lessons by id`() {
            val result = teacherLessonEntityRepository.findAllByTeacherIdOrderByIndex(teacherId)
            assertEquals(teacherSavedLessons, result)
        }
    }

    @DisplayName("when deleting teacher lessons")
    @Nested
    inner class TeacherProfileDeleteTest {

        @Test
        fun `should delete teacher lessons by id`() {
            assertEquals(teacherSavedLessons, teacherLessonEntityRepository.findAllByTeacherIdOrderByIndex(teacherId))
            teacherLessonEntityRepository.deleteAllByTeacherId(teacherId)
            assertEquals(listOf<TeacherLessonEntity>(), teacherLessonEntityRepository.findAllByTeacherIdOrderByIndex(teacherId))
        }
    }
}