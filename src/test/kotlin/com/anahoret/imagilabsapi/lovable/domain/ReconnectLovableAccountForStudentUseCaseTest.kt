package com.anahoret.imagilabsapi.lovable.domain

import arrow.core.Either
import arrow.core.left
import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomAccessService
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomPermissions
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.common.testStudent
import com.anahoret.imagilabsapi.common.testTeacher
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

class ReconnectLovableAccountForStudentUseCaseTest {

    private val classroomId = UUID.randomUUID()

    private val classroomAccessService: ClassroomAccessService = mockk()
    private val classroomService: ClassroomService = mockk()
    private val connectLovableAccountToUserUseCase: ConnectLovableAccountToUserUseCase = mockk()
    private val studentProfileService: StudentProfileService = mockk()

    private val reconnectLovableAccountForStudentUseCase = ReconnectLovableAccountForStudentUseCaseImpl(
        classroomAccessService,
        classroomService,
        connectLovableAccountToUserUseCase,
        studentProfileService
    )

    @Test
    fun `reconnect returns new LovableAccount when successful`() {
        val teacher = testTeacher()
        val student = testStudent()
        val classroom = Classroom(
            id = classroomId,
            name = "Test Classroom",
            accessCode = "ABC123",
            studentsCount = 1L,
            projectsCount = 0L,
            teacherId = teacher.id,
            teachersCount = 1L,
            blocked = false,
            ClassroomPermissions(true)
        )
        val expectedAccount = LovableAccount(student.id, "s1", "student1", "student1@example.com", "password123")

        every { studentProfileService.getStudentById(student.id) } returns student
        every { classroomService.getById(classroomId) } returns classroom
        every { classroomAccessService.canUpdateClassroom(teacher, classroom) } returns true
        every { connectLovableAccountToUserUseCase.connect(student) } returns Either.Right(expectedAccount)

        val result = reconnectLovableAccountForStudentUseCase.reconnect(teacher, student.id, classroomId)

        assertTrue(result.isRight())
        assertEquals(expectedAccount, result.getOrNull())
        verify { studentProfileService.getStudentById(student.id) }
        verify { classroomService.getById(classroomId) }
        verify { classroomAccessService.canUpdateClassroom(teacher, classroom) }
        verify { connectLovableAccountToUserUseCase.connect(student) }
    }

    @Test
    fun `reconnect returns NotFoundError when student not found`() {
        val teacher = testTeacher()
        val studentId = UUID.randomUUID()

        every { studentProfileService.getStudentById(studentId) } returns null

        val result = reconnectLovableAccountForStudentUseCase.reconnect(teacher, studentId, classroomId)

        assertTrue(result.isLeft())
        assertTrue(result.leftOrNull() is NotFoundError)
        assertEquals("STUDENT_NOT_FOUND", (result.leftOrNull() as NotFoundError).message)
        verify { studentProfileService.getStudentById(studentId) }
    }

    @Test
    fun `reconnect returns NotFoundError when classroom not found`() {
        val teacher = testTeacher()
        val student = testStudent()

        every { studentProfileService.getStudentById(student.id) } returns student
        every { classroomService.getById(classroomId) } returns null

        val result = reconnectLovableAccountForStudentUseCase.reconnect(teacher, student.id, classroomId)

        assertTrue(result.isLeft())
        assertTrue(result.leftOrNull() is NotFoundError)
        assertEquals("CLASSROOM_NOT_FOUND", (result.leftOrNull() as NotFoundError).message)
        verify { studentProfileService.getStudentById(student.id) }
        verify { classroomService.getById(classroomId) }
    }

    @Test
    fun `reconnect returns AccessDeniedError when teacher cannot update classroom`() {
        val teacher = testTeacher()
        val student = testStudent()
        val classroom = Classroom(
            id = classroomId,
            name = "Test Classroom",
            accessCode = "ABC123",
            studentsCount = 1L,
            projectsCount = 0L,
            teacherId = UUID.randomUUID(),
            teachersCount = 1L,
            blocked = false,
            ClassroomPermissions(true)
        )

        every { studentProfileService.getStudentById(student.id) } returns student
        every { classroomService.getById(classroomId) } returns classroom
        every { classroomAccessService.canUpdateClassroom(teacher, classroom) } returns false

        val result = reconnectLovableAccountForStudentUseCase.reconnect(teacher, student.id, classroomId)

        assertTrue(result.isLeft())
        assertTrue(result.leftOrNull() is AccessDeniedError)
        assertEquals("ACCESS_TO_CLASSROOM_DENIED", (result.leftOrNull() as AccessDeniedError).message)
        verify { studentProfileService.getStudentById(student.id) }
        verify { classroomService.getById(classroomId) }
        verify { classroomAccessService.canUpdateClassroom(teacher, classroom) }
    }

    @Test
    fun `reconnect returns AccessDenied when classroom is blocked`() {
        val teacher = testTeacher()
        val student = testStudent()
        val classroom = Classroom(
            id = classroomId,
            name = "Test Classroom",
            accessCode = "ABC123",
            studentsCount = 1L,
            projectsCount = 0L,
            teacherId = teacher.id,
            teachersCount = 1L,
            blocked = true,
            ClassroomPermissions(true)
        )

        every { studentProfileService.getStudentById(student.id) } returns student
        every { classroomService.getById(classroomId) } returns classroom

        val result = reconnectLovableAccountForStudentUseCase.reconnect(teacher, student.id, classroomId)

        assertEquals(AccessDeniedError("SUBSCRIPTION_REQUIRED").left(), result)
    }

    @Test
    fun `reconnect propagates connection errors from ConnectLovableAccountToUserUseCase`() {
        val teacher = testTeacher()
        val student = testStudent()
        val classroom = Classroom(
            id = classroomId,
            name = "Test Classroom",
            accessCode = "ABC123",
            studentsCount = 1L,
            projectsCount = 0L,
            teacherId = teacher.id,
            teachersCount = 1L,
            blocked = false,
            ClassroomPermissions(true)
        )
        val expectedError = OutOfLovableAccountsError()

        every { studentProfileService.getStudentById(student.id) } returns student
        every { classroomService.getById(classroomId) } returns classroom
        every { classroomAccessService.canUpdateClassroom(teacher, classroom) } returns true
        every { connectLovableAccountToUserUseCase.connect(student) } returns Either.Left(expectedError)

        val result = reconnectLovableAccountForStudentUseCase.reconnect(teacher, student.id, classroomId)

        assertTrue(result.isLeft())
        assertEquals(expectedError, result.leftOrNull())
        verify { studentProfileService.getStudentById(student.id) }
        verify { classroomService.getById(classroomId) }
        verify { classroomAccessService.canUpdateClassroom(teacher, classroom) }
        verify { connectLovableAccountToUserUseCase.connect(student) }
    }

    @Test
    fun `reconnect propagates MaxNumberOfConnectedAccountsExceededError from ConnectLovableAccountToUserUseCase`() {
        val teacher = testTeacher()
        val student = testStudent()
        val classroom = Classroom(
            id = classroomId,
            name = "Test Classroom",
            accessCode = "ABC123",
            studentsCount = 1L,
            projectsCount = 0L,
            teacherId = teacher.id,
            teachersCount = 1L,
            blocked = false,
            ClassroomPermissions(true)
        )
        val expectedError = MaxNumberOfConnectedAccountsExceededError()

        every { studentProfileService.getStudentById(student.id) } returns student
        every { classroomService.getById(classroomId) } returns classroom
        every { classroomAccessService.canUpdateClassroom(teacher, classroom) } returns true
        every { connectLovableAccountToUserUseCase.connect(student) } returns Either.Left(expectedError)

        val result = reconnectLovableAccountForStudentUseCase.reconnect(teacher, student.id, classroomId)

        assertTrue(result.isLeft())
        assertEquals(expectedError, result.leftOrNull())
        verify { studentProfileService.getStudentById(student.id) }
        verify { classroomService.getById(classroomId) }
        verify { classroomAccessService.canUpdateClassroom(teacher, classroom) }
        verify { connectLovableAccountToUserUseCase.connect(student) }
    }
}
