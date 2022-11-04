package com.anahoret.imagilabsapi.teachers.storage

import com.anahoret.imagilabsapi.spring.ImagiLabsDatabaseTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID

@ImagiLabsDatabaseTest
@DisplayName("Teacher Profile entity repository")
class TeacherProfileEntityRepositoryTest {

    @Autowired
    lateinit var teacherProfileEntityRepository: TeacherProfileEntityRepository

    @DisplayName("when fetching teacher profile")
    @Nested
    inner class TeacherProfileFetchTest {

        private lateinit var teacherProfileEntity: TeacherProfileEntity
        private lateinit var teacherId: UUID

        @BeforeEach
        fun setup() {
            setupTeacher()
        }

        private fun setupTeacher() {
            teacherProfileEntity = teacherProfileEntityRepository.save(
                TeacherProfileEntity("teacher1@mail.com", "", "Teacher", "Edu", "Sweden", "imagi", "other", true)
            )
            teacherId = teacherProfileEntity.id!!
        }

        @Test
        fun `should find teacher by id`() {
            val result = teacherProfileEntityRepository.findByIdOrNull(teacherId)
            assertEquals(teacherProfileEntity, result)
        }
    }
}