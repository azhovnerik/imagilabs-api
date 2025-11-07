package com.anahoret.imagilabsapi.lovable.domain.accountcards

import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomAccessService
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.lovable.domain.DownloadStudentsLovableAccountsRequest
import com.anahoret.imagilabsapi.lovable.domain.GetLovableCredentialsForClassroomUseCase
import com.anahoret.imagilabsapi.lovable.domain.LovableAccount
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.util.*

class StudentLovableAccountCardsGeneratorImplTest {

    private val classroomService = mockk<ClassroomService>()
    private val classroomAccessService = mockk<ClassroomAccessService>()
    private val getLovableCredentialsForClassroomUseCase = mockk<GetLovableCredentialsForClassroomUseCase>()
    private val pdfGenerator = mockk<StudentLovableAccountCardsPdfGenerator>()
    private val csvGenerator = mockk<StudentLovableAccountCardsCsvGenerator>()

    private val cardsGenerator = StudentLovableAccountCardsGeneratorImpl(
        classroomService = classroomService,
        classroomAccessService = classroomAccessService,
        getLovableCredentialsForClassroomUseCase = getLovableCredentialsForClassroomUseCase,
        studentLovableAccountCardsPdfGenerator = pdfGenerator,
        studentLovableAccountCardsCsvGenerator = csvGenerator
    )

    private val teacherProfile = mockk<TeacherProfile>(relaxed = true)
    private val classroomId = UUID.randomUUID()
    private val classroom = mockk<Classroom>(relaxed = true) {
        every { name } returns "Test Classroom"
    }

    @BeforeEach
    fun setup() {
        clearMocks(
            classroomService,
            classroomAccessService,
            getLovableCredentialsForClassroomUseCase,
            pdfGenerator,
            csvGenerator
        )
    }

    @Test
    fun `should return NotFoundError when classroom does not exist`() {
        val downloadRequest = DownloadStudentsLovableAccountsRequest(
            format = StudentsLovableAccountCardsFormat.PDF,
            studentIds = null
        )

        every { classroomService.getById(classroomId) } returns null

        val result = cardsGenerator.generate(teacherProfile, classroomId, downloadRequest)

        assertTrue(result.isLeft())
        val error = result.leftOrNull()
        assertTrue(error is NotFoundError)
        assertEquals("CLASSROOM_NOT_FOUND", (error as NotFoundError).message)
    }

    @Test
    fun `should return AccessDeniedError when teacher cannot access classroom`() {
        val downloadRequest = DownloadStudentsLovableAccountsRequest(
            format = StudentsLovableAccountCardsFormat.PDF,
            studentIds = null
        )

        every { classroomService.getById(classroomId) } returns classroom
        every { classroomAccessService.canListStudentCredentials(teacherProfile, classroom) } returns false

        val result = cardsGenerator.generate(teacherProfile, classroomId, downloadRequest)

        assertTrue(result.isLeft())
        val error = result.leftOrNull()
        assertTrue(error is AccessDeniedError)
        assertEquals("ACCESS_TO_CLASSROOM_DENIED", (error as AccessDeniedError).message)
    }

    @Test
    fun `should generate PDF file successfully for all students`() {
        val accounts = listOf(
            LovableAccount(UUID.randomUUID(), "s1", "user1", "user1@test.com", "pass1"),
            LovableAccount(UUID.randomUUID(), "s2", "user2", "user2@test.com", "pass2")
        )
        val downloadRequest = DownloadStudentsLovableAccountsRequest(
            format = StudentsLovableAccountCardsFormat.PDF,
            studentIds = null
        )
        val mockInputStream = ByteArrayInputStream("pdf content".toByteArray())

        every { classroomService.getById(classroomId) } returns classroom
        every { classroomAccessService.canListStudentCredentials(teacherProfile, classroom) } returns true
        every {
            getLovableCredentialsForClassroomUseCase.getCredentials(
                teacherProfile,
                classroomId
            )
        } returns accounts.right()
        every { pdfGenerator.generate(accounts) } returns mockInputStream

        val result = cardsGenerator.generate(teacherProfile, classroomId, downloadRequest)

        assertTrue(result.isRight())
        val cardsFile = result.getOrNull()!!
        assertEquals("lovable-Test Classroom-students.pdf", cardsFile.fileName)
        assertEquals(StudentsLovableAccountCardsFormat.PDF, cardsFile.format)
        assertEquals(mockInputStream, cardsFile.inputStream)

        verify(exactly = 1) { pdfGenerator.generate(accounts) }
        verify(exactly = 0) { csvGenerator.generate(any()) }
    }

    @Test
    fun `should generate CSV file successfully for all students`() {
        val accounts = listOf(
            LovableAccount(UUID.randomUUID(), "s1", "user1", "user1@test.com", "pass1"),
            LovableAccount(UUID.randomUUID(), "s2", "user2", "user2@test.com", "pass2")
        )
        val downloadRequest = DownloadStudentsLovableAccountsRequest(
            format = StudentsLovableAccountCardsFormat.CSV,
            studentIds = null
        )
        val mockInputStream = ByteArrayInputStream("csv content".toByteArray())

        every { classroomService.getById(classroomId) } returns classroom
        every { classroomAccessService.canListStudentCredentials(teacherProfile, classroom) } returns true
        every {
            getLovableCredentialsForClassroomUseCase.getCredentials(
                teacherProfile,
                classroomId
            )
        } returns accounts.right()
        every { csvGenerator.generate(accounts) } returns mockInputStream

        val result = cardsGenerator.generate(teacherProfile, classroomId, downloadRequest)

        assertTrue(result.isRight())
        val cardsFile = result.getOrNull()!!
        assertEquals("lovable-Test Classroom-students.csv", cardsFile.fileName)
        assertEquals(StudentsLovableAccountCardsFormat.CSV, cardsFile.format)
        assertEquals(mockInputStream, cardsFile.inputStream)

        verify(exactly = 1) { csvGenerator.generate(accounts) }
        verify(exactly = 0) { pdfGenerator.generate(any()) }
    }

    @Test
    fun `should filter students when specific studentIds are provided`() {
        val userId1 = UUID.randomUUID()
        val userId2 = UUID.randomUUID()
        val userId3 = UUID.randomUUID()

        val allAccounts = listOf(
            LovableAccount(userId1, "s1", "user1", "user1@test.com", "pass1"),
            LovableAccount(userId2, "s2", "user2", "user2@test.com", "pass2"),
            LovableAccount(userId3, "s3", "user3", "user3@test.com", "pass3")
        )

        val selectedStudentIds = setOf(userId1, userId3)
        val downloadRequest = DownloadStudentsLovableAccountsRequest(
            format = StudentsLovableAccountCardsFormat.PDF,
            studentIds = selectedStudentIds
        )

        val expectedFilteredAccounts = listOf(
            allAccounts[0], // user1
            allAccounts[2]  // user3
        )
        val mockInputStream = ByteArrayInputStream("pdf content".toByteArray())

        every { classroomService.getById(classroomId) } returns classroom
        every { classroomAccessService.canListStudentCredentials(teacherProfile, classroom) } returns true
        every {
            getLovableCredentialsForClassroomUseCase.getCredentials(
                teacherProfile,
                classroomId
            )
        } returns allAccounts.right()
        every { pdfGenerator.generate(expectedFilteredAccounts) } returns mockInputStream

        val result = cardsGenerator.generate(teacherProfile, classroomId, downloadRequest)

        assertTrue(result.isRight())
        verify(exactly = 1) { pdfGenerator.generate(expectedFilteredAccounts) }
    }

    @Test
    fun `should handle error from GetLovableCredentialsForClassroomUseCase`() {
        val downloadRequest = DownloadStudentsLovableAccountsRequest(
            format = StudentsLovableAccountCardsFormat.PDF,
            studentIds = null
        )
        val error = NotFoundError("CREDENTIALS_NOT_FOUND")

        every { classroomService.getById(classroomId) } returns classroom
        every { classroomAccessService.canListStudentCredentials(teacherProfile, classroom) } returns true
        every {
            getLovableCredentialsForClassroomUseCase.getCredentials(
                teacherProfile,
                classroomId
            )
        } returns error.left()

        val result = cardsGenerator.generate(teacherProfile, classroomId, downloadRequest)

        assertTrue(result.isLeft())
        assertEquals(error, result.leftOrNull())

        verify(exactly = 0) { pdfGenerator.generate(any()) }
        verify(exactly = 0) { csvGenerator.generate(any()) }
    }

    @Test
    fun `should generate empty file when no students match filter`() {
        val allAccounts = listOf(
            LovableAccount(UUID.randomUUID(), "s1", "user1", "user1@test.com", "pass1")
        )

        val nonMatchingStudentIds = setOf(UUID.randomUUID())
        val downloadRequest = DownloadStudentsLovableAccountsRequest(
            format = StudentsLovableAccountCardsFormat.PDF,
            studentIds = nonMatchingStudentIds
        )
        val mockInputStream = ByteArrayInputStream("pdf content".toByteArray())

        every { classroomService.getById(classroomId) } returns classroom
        every { classroomAccessService.canListStudentCredentials(teacherProfile, classroom) } returns true
        every {
            getLovableCredentialsForClassroomUseCase.getCredentials(
                teacherProfile,
                classroomId
            )
        } returns allAccounts.right()
        every { pdfGenerator.generate(emptyList()) } returns mockInputStream

        val result = cardsGenerator.generate(teacherProfile, classroomId, downloadRequest)

        assertTrue(result.isRight())
        verify(exactly = 1) { pdfGenerator.generate(emptyList()) }
    }

    @Test
    fun `should handle classroom with special characters in name`() {
        val classroomWithSpecialName = mockk<Classroom> {
            every { name } returns "Test Classroom & More!"
        }
        val downloadRequest = DownloadStudentsLovableAccountsRequest(
            format = StudentsLovableAccountCardsFormat.PDF,
            studentIds = null
        )
        val mockInputStream = ByteArrayInputStream("pdf content".toByteArray())
        val accounts = listOf(LovableAccount(UUID.randomUUID(), "s1", "user1", "user1@test.com", "pass1"))

        every { classroomService.getById(classroomId) } returns classroomWithSpecialName
        every {
            classroomAccessService.canListStudentCredentials(
                teacherProfile,
                classroomWithSpecialName
            )
        } returns true
        every {
            getLovableCredentialsForClassroomUseCase.getCredentials(
                teacherProfile,
                classroomId
            )
        } returns accounts.right()
        every { pdfGenerator.generate(accounts) } returns mockInputStream

        val result = cardsGenerator.generate(teacherProfile, classroomId, downloadRequest)

        assertTrue(result.isRight())
        val cardsFile = result.getOrNull()!!
        assertEquals("lovable-Test Classroom & More!-students.pdf", cardsFile.fileName)
    }
}
