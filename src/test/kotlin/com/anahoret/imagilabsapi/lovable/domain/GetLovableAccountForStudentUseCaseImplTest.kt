package com.anahoret.imagilabsapi.lovable.domain

import arrow.core.Either
import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomAccessService
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.common.testTeacher
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class GetLovableAccountForStudentUseCaseImplTest {

    private lateinit var classroomAccessService: ClassroomAccessService
    private lateinit var classroomService: ClassroomService
    private lateinit var lovableAccountService: LovableAccountService
    private lateinit var studentProfileService: StudentProfileService
    private lateinit var useCase: GetLovableAccountForStudentUseCase

    @BeforeEach
    fun setUp() {
        classroomAccessService = mockk()
        classroomService = mockk()
        lovableAccountService = mockk()
        studentProfileService = mockk()
        useCase = GetLovableAccountForStudentUseCaseImpl(
            classroomAccessService,
            classroomService,
            lovableAccountService,
            studentProfileService
        )
    }

    @Test
    fun `returns NotFound when student does not exist`() {
        val teacher = testTeacher()
        val studentId = UUID.randomUUID()
        every { studentProfileService.getStudentById(studentId) } returns null

        val result = useCase.get(teacher, studentId)

        assertTrue(result is Either.Left)
        val error = (result as Either.Left).value
        assertTrue(error is NotFoundError)
        assertEquals("STUDENT_NOT_FOUND", (error as NotFoundError).message)
    }

    @Test
    fun `returns NotFound when student's classroom does not exist`() {
        val teacher = testTeacher()
        val studentId = UUID.randomUUID()
        val classroomId = UUID.randomUUID()
        val student = StudentProfile(studentId, "Student Name", "student1", 123L, classroomId)

        every { studentProfileService.getStudentById(studentId) } returns student
        every { classroomService.getById(classroomId) } returns null

        val result = useCase.get(teacher, studentId)

        assertTrue(result is Either.Left)
        val error = (result as Either.Left).value
        assertTrue(error is NotFoundError)
        assertEquals("CLASSROOM_NOT_FOUND", (error as NotFoundError).message)
    }

    @Test
    fun `returns AccessDenied when teacher cannot list student credentials`() {
        val teacher = testTeacher()
        val studentId = UUID.randomUUID()
        val classroomId = UUID.randomUUID()
        val student = StudentProfile(studentId, "Student Name", "student1", 123L, classroomId)
        val classroom = Classroom(classroomId, "Test Classroom", "TC123", 0, 0, UUID.randomUUID(), 1, blocked = false)

        every { studentProfileService.getStudentById(studentId) } returns student
        every { classroomService.getById(classroomId) } returns classroom
        every { classroomAccessService.canListStudentCredentials(teacher, classroom) } returns false

        val result = useCase.get(teacher, studentId)

        assertTrue(result is Either.Left)
        val error = (result as Either.Left).value
        assertTrue(error is AccessDeniedError)
        assertEquals("ACCESS_TO_CLASSROOM_DENIED", (error as AccessDeniedError).message)
    }

    @Test
    fun `returns LovableAccount when found and access is granted`() {
        val teacher = testTeacher()
        val studentId = UUID.randomUUID()
        val classroomId = UUID.randomUUID()
        val student = StudentProfile(studentId, "Student Name", "student1", 123L, classroomId)
        val classroom = Classroom(classroomId, "Test Classroom", "TC123", 0, 0, teacher.id, 1, blocked = false)
        val lovableAccount = LovableAccount(studentId, "student1", "student1@example.com", "password123")

        every { studentProfileService.getStudentById(studentId) } returns student
        every { classroomService.getById(classroomId) } returns classroom
        every { classroomAccessService.canListStudentCredentials(teacher, classroom) } returns true
        every { lovableAccountService.getActive(student) } returns lovableAccount

        val result = useCase.get(teacher, studentId)

        assertTrue(result is Either.Right)
        val account = (result as Either.Right).value
        assertEquals(lovableAccount, account)
        verify { lovableAccountService.getActive(student) }
    }

    @Test
    fun `returns NotFound when student has no active Lovable account`() {
        val teacher = testTeacher()
        val studentId = UUID.randomUUID()
        val classroomId = UUID.randomUUID()
        val student = StudentProfile(studentId, "Student Name", "student1", 123L, classroomId)
        val classroom = Classroom(classroomId, "Test Classroom", "TC123", 0, 0, teacher.id, 1, blocked = false)

        every { studentProfileService.getStudentById(studentId) } returns student
        every { classroomService.getById(classroomId) } returns classroom
        every { classroomAccessService.canListStudentCredentials(teacher, classroom) } returns true
        every { lovableAccountService.getActive(student) } returns null

        val result = useCase.get(teacher, studentId)

        assertTrue(result is Either.Left)
        val error = (result as Either.Left).value
        assertTrue(error is NotFoundError)
        assertEquals("LOVABLE_ACCOUNT_NOT_FOUND", (error as NotFoundError).message)
        verify { lovableAccountService.getActive(student) }
    }
}
