package com.anahoret.imagilabsapi.students.storage

import com.anahoret.imagilabsapi.classrooms.storage.ClassroomEntity
import com.anahoret.imagilabsapi.classrooms.storage.ClassroomEntityRepository
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntity
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntityRepository

import com.anahoret.imagilabsapi.spring.ImagiLabsDatabaseTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

@ImagiLabsDatabaseTest
@DisplayName("Student Profile entity repository")
class StudentProfileEntityRepositoryTest {

    @Autowired
    lateinit var teacherProfileEntityRepository: TeacherProfileEntityRepository

    @Autowired
    lateinit var studentProfileEntityRepository: StudentProfileEntityRepository

    @Autowired
    lateinit var classroomEntityRepository: ClassroomEntityRepository

    private lateinit var teacherId: UUID
    private lateinit var classroomId: UUID
    private lateinit var classroomStudentIds: List<UUID>
    private lateinit var sansaId: UUID
    private lateinit var brandonId: UUID

    @BeforeEach
    fun setup() {
        setupTeacher()
        setupClassroom()
        setupStudents()
    }

    private fun setupTeacher() {
        teacherId = teacherProfileEntityRepository.save(
            TeacherProfileEntity("teacher5@mail.com", "", "Eddard", "Stark", "", "", "", true)
        ).id!!
    }

    private fun setupClassroom() {
        val classroom = classroomEntityRepository.save(ClassroomEntity("", "access1", teacherId))
        classroomId = classroom.id!!
    }

    private fun setupStudents() {
        val classroomStudents = listOf(
            StudentProfileEntity("Rob Stark", "rstark", "rstark", classroomId),
            StudentProfileEntity("Sansa Stark", "sstark", "sstark", classroomId),
        ).let(studentProfileEntityRepository::saveAll)
        sansaId = classroomStudents.find { it.username == "sstark" }?.id!!
        brandonId = classroomStudents.find { it.username == "rstark" }?.id!!
        classroomStudentIds = classroomStudents.map { it.id!! }
    }

    @DisplayName("when deleting students")
    @Nested
    inner class StudentProfileDeleteTest {

        @Test
        fun `should delete students by ids`() {
            assertEquals(classroomStudentIds, studentProfileEntityRepository.findAllByClassroomId(classroomId).map { it.id })
            studentProfileEntityRepository.deleteByIdIn(classroomStudentIds)
            assertEquals(listOf<UUID>(), studentProfileEntityRepository.findAllByClassroomId(classroomId).map { it.id })
        }
    }
}