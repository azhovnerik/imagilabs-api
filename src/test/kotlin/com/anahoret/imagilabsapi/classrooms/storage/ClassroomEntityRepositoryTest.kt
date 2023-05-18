package com.anahoret.imagilabsapi.classrooms.storage

import com.anahoret.imagilabsapi.spring.ImagiLabsDatabaseTest
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntity
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntityRepository
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
@DisplayName("Classroom entity repository")
class ClassroomEntityRepositoryTest {

    @Autowired
    lateinit var teacherProfileEntityRepository: TeacherProfileEntityRepository

    @Autowired
    lateinit var classroomEntityRepository: ClassroomEntityRepository

    private lateinit var teacherId: UUID
    private lateinit var classroomId: UUID
    private lateinit var classroomEntity: ClassroomEntity

    @BeforeEach
    fun setup() {
        setupTeacher()
        setupClassroom()
    }

    private fun setupTeacher() {
        teacherId = teacherProfileEntityRepository.save(
            TeacherProfileEntity("teacher4@mail.com", "", "Eddard", "Stark", "", "", "", true)
        ).id!!
    }

    private fun setupClassroom() {
        classroomEntity = classroomEntityRepository.save(ClassroomEntity("", "access1", teacherId))
        classroomId = classroomEntity.id!!
    }

    @DisplayName("when deleting classroom")
    @Nested
    inner class ClassroomDeleteTest {

        @Test
        fun `should delete classroom by id`() {
            assertEquals(classroomEntity, classroomEntityRepository.findByIdOrNull(classroomId))
            classroomEntityRepository.deleteById(classroomId)
            assertNull(classroomEntityRepository.findByIdOrNull(classroomId))
        }
    }
}
