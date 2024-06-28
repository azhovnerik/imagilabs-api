package com.anahoret.imagilabsapi.teachers.storage

import com.anahoret.imagilabsapi.spring.ImagiLabsDatabaseTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import java.util.*

@ImagiLabsDatabaseTest
@DisplayName("Teacher Profile entity repository")
class TeacherProfileEntityRepositoryTest {

    @Autowired
    lateinit var teacherProfileEntityRepository: TeacherProfileEntityRepository

    private lateinit var teacherProfileEntity: TeacherProfileEntity
    private lateinit var teacherId: UUID


    @BeforeEach
    fun setup() {
        setupTeacher()
    }

    private fun setupTeacher() {
        teacherProfileEntity = teacherProfileEntityRepository.save(
            TeacherProfileEntity("teacher1@mail.com", "", "Teacher", "Edu", "Sweden", "imagi", "other", "", true, 3)
        )
        teacherId = teacherProfileEntity.id!!
    }
    @DisplayName("when fetching teacher profile")
    @Nested
    inner class TeacherProfileFetchTest {

        @Test
        fun `should find teacher by id`() {
            val result = teacherProfileEntityRepository.findByIdOrNull(teacherId)
            assertEquals(teacherProfileEntity, result)
        }
    }

    @DisplayName("when deleting teacher profile")
    @Nested
    inner class TeacherProfileDeleteTest {

        @Test
        fun `should delete teacher by id`() {
            assertEquals(teacherProfileEntity, teacherProfileEntityRepository.findByIdOrNull(teacherId))
            teacherProfileEntityRepository.deleteById(teacherId)
            assertNull(teacherProfileEntityRepository.findByIdOrNull(teacherId))
        }
    }
}
