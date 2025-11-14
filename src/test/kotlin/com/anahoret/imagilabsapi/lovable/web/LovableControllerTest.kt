package com.anahoret.imagilabsapi.lovable.web

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.common.testStudent
import com.anahoret.imagilabsapi.common.testTeacher
import com.anahoret.imagilabsapi.lovable.domain.*
import com.anahoret.imagilabsapi.lovable.domain.accountcards.StudentLovableAccountCardsFile
import com.anahoret.imagilabsapi.lovable.domain.accountcards.StudentLovableAccountCardsGenerator
import com.anahoret.imagilabsapi.lovable.domain.accountcards.StudentsLovableAccountCardsFormat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.io.ByteArrayInputStream
import java.util.*

class LovableControllerTest {

    private val classroomId = UUID.randomUUID()

    private val connectLovableAccountToUserUseCase: ConnectLovableAccountToUserUseCase = mockk()
    private val getLovableAccountForUserUseCase: GetLovableAccountForUserUseCase = mockk()
    private val getLovableAccountForStudentUseCase: GetLovableAccountForStudentUseCase = mockk()
    private val reconnectLovableAccountForStudentUseCase: ReconnectLovableAccountForStudentUseCase = mockk()
    private val enableLovableIntegrationForClassroomUseCase: EnableLovableIntegrationForClassroomUseCase = mockk()
    private val setPausedLovableIntegrationForClassroomUseCase: SetPausedLovableIntegrationForClassroomUseCase =
        mockk(relaxed = true)
    private val getLovableCredentialsForClassroomUseCase: GetLovableCredentialsForClassroomUseCase = mockk()
    private val getLovableIntegrationForClassroomUseCase: GetLovableIntegrationForClassroomUseCase = mockk()
    private val studentLovableAccountCardsGenerator: StudentLovableAccountCardsGenerator = mockk()
    private val controller =
        LovableController(
            connectLovableAccountToUserUseCase,
            getLovableAccountForUserUseCase,
            getLovableAccountForStudentUseCase,
            reconnectLovableAccountForStudentUseCase,
            enableLovableIntegrationForClassroomUseCase,
            setPausedLovableIntegrationForClassroomUseCase,
            getLovableCredentialsForClassroomUseCase,
            getLovableIntegrationForClassroomUseCase,
            studentLovableAccountCardsGenerator
        )

    @Test
    fun `connectTeacherProfile returns 200 with body on success`() {
        val teacher = testTeacher()
        val account = LovableAccount(UUID.randomUUID(), "s", "u", "a@x.com", "p")
        every { connectLovableAccountToUserUseCase.connect(teacher) } returns Either.Right(account)

        val response = controller.connectTeacherProfile(teacher)

        assertTrue(response.statusCode.is2xxSuccessful)
        assertEquals(200, response.statusCode.value())
        val body = response.body
        assertNotNull(body)
    }

    @Test
    fun `connectTeacherProfile maps errors to non-2xx`() {
        val teacher = testTeacher()
        every { connectLovableAccountToUserUseCase.connect(teacher) } returns Either.Left(
            MaxNumberOfConnectedAccountsExceededError()
        )

        val response = controller.connectTeacherProfile(teacher)

        assertTrue(!response.statusCode.is2xxSuccessful)
        assertTrue(response.statusCode.isError)
        assertNotNull(response.body)
    }

    @Test
    fun `getLovableAccount returns 200 with body when found for teacher`() {
        val teacher = testTeacher()
        val account = LovableAccount(UUID.randomUUID(), "s", "u", "b@x.com", "pwd")
        every { getLovableAccountForUserUseCase.get(teacher) } returns account

        val response = controller.getLovableAccount(teacher, classroomId = null)

        assertEquals(200, response.statusCode.value())
        assertTrue(response.statusCode.is2xxSuccessful)
        assertNotNull(response.body)
    }

    @Test
    fun `getLovableAccount returns 200 with body when found for student`() {
        val student = testStudent()
        val account = LovableAccount(UUID.randomUUID(), "s", "u", "b@x.com", "pwd")
        every { getLovableAccountForUserUseCase.get(student, classroomId) } returns account

        val response = controller.getLovableAccount(student, classroomId)

        assertEquals(200, response.statusCode.value())
        assertTrue(response.statusCode.is2xxSuccessful)
        assertNotNull(response.body)
    }

    @Test
    fun `getLovableAccount returns 404 when not found`() {
        val teacher = testTeacher()
        every { getLovableAccountForUserUseCase.get(teacher) } returns null

        val response = controller.getLovableAccount(teacher, classroomId = null)

        assertEquals(404, response.statusCode.value())
        assertTrue(response.body == null)
    }

    @Test
    fun `enableIntegrationForClassroom returns 200 on success`() {
        val teacher = testTeacher()
        val classroomId = UUID.randomUUID()
        every { enableLovableIntegrationForClassroomUseCase.enable(teacher, classroomId) } returns Either.Right(mockk())

        val response = controller.enableIntegrationForClassroom(teacher, classroomId)

        assertEquals(200, response.statusCode.value())
        assertTrue(response.statusCode.is2xxSuccessful)
    }

    @Test
    fun `enableIntegrationForClassroom maps errors to non-2xx`() {
        val teacher = testTeacher()
        val classroomId = UUID.randomUUID()
        every { enableLovableIntegrationForClassroomUseCase.enable(teacher, classroomId) } returns Either.Left(
            MaxNumberOfConnectedAccountsExceededError()
        )

        val response = controller.enableIntegrationForClassroom(teacher, classroomId)

        assertTrue(response.statusCode.isError)
        assertNotEquals(200, response.statusCode.value())
        assertNotNull(response.body)
    }

    @Test
    fun `setPausedIntegrationForClassroom returns 200 and passes args`() {
        val teacher = testTeacher()
        val classroomId = UUID.randomUUID()
        val request = LovableController.SetPausedRequest(true)

        val response = controller.setPausedIntegrationForClassroom(teacher, classroomId, request)

        assertEquals(200, response.statusCode.value())
        verify { setPausedLovableIntegrationForClassroomUseCase.setPaused(teacher, classroomId, true) }
    }

    @Test
    fun `getStudentsCredentialsForClassroom returns 200 with body on success`() {
        val teacher = testTeacher()
        val classroomId = UUID.randomUUID()
        val list = listOf(LovableAccount(UUID.randomUUID(), "s", "u", "c@x.com", "pass"))
        every { getLovableCredentialsForClassroomUseCase.getCredentials(teacher, classroomId) } returns Either.Right(
            list
        )

        val response = controller.getStudentsCredentialsForClassroom(teacher, classroomId)

        assertEquals(200, response.statusCode.value())
        assertTrue(response.statusCode.is2xxSuccessful)
        assertNotNull(response.body)
    }

    @Test
    fun `getStudentsCredentialsForClassroom maps errors to non-2xx`() {
        val teacher = testTeacher()
        val classroomId = UUID.randomUUID()
        every { getLovableCredentialsForClassroomUseCase.getCredentials(teacher, classroomId) } returns Either.Left(
            MaxNumberOfConnectedAccountsExceededError()
        )

        val response = controller.getStudentsCredentialsForClassroom(teacher, classroomId)

        assertTrue(response.statusCode.isError)
        assertNotNull(response.body)
    }

    @Test
    fun `getIntegrationForClassroom returns 200 on success`() {
        val teacher = testTeacher()
        val classroomId = UUID.randomUUID()
        every { getLovableIntegrationForClassroomUseCase.get(teacher, classroomId) } returns Either.Right(
            LovableClassroom(classroomId, true, false)
        )

        val response = controller.getIntegrationForClassroom(teacher, classroomId)

        assertEquals(200, response.statusCode.value())
        assertTrue(response.statusCode.is2xxSuccessful)
        assertEquals(classroomId, response.body?.payload?.classroomId)
    }

    @Test
    fun `getIntegrationForClassroom maps NotFound to 404`() {
        val student = testStudent()
        val classroomId = UUID.randomUUID()
        every { getLovableIntegrationForClassroomUseCase.get(student, classroomId) } returns Either.Left(
            NotFoundError("LOVABLE_INTEGRATION_NOT_FOUND")
        )

        val response = controller.getIntegrationForClassroom(student, classroomId)

        assertEquals(404, response.statusCode.value())
        assertTrue(response.statusCode.isError)
        assertNotNull(response.body)
    }

    @Test
    fun `getIntegrationForClassroom maps AccessDenied to 403`() {
        val student = testStudent()
        val classroomId = UUID.randomUUID()
        every { getLovableIntegrationForClassroomUseCase.get(student, classroomId) } returns Either.Left(
            AccessDeniedError("ACCESS_TO_CLASSROOM_DENIED")
        )

        val response = controller.getIntegrationForClassroom(student, classroomId)

        assertEquals(403, response.statusCode.value())
        assertTrue(response.statusCode.isError)
        assertNotNull(response.body)
    }

    @Test
    fun `getStudentsLovableAccountsForClassroom returns PDF file with correct headers on success`() {
        val teacher = testTeacher()
        val classroomId = UUID.randomUUID()
        val downloadRequest = DownloadStudentsLovableAccountsRequest(
            format = StudentsLovableAccountCardsFormat.PDF,
            studentIds = null
        )
        val pdfInputStream = ByteArrayInputStream("pdf content".toByteArray())
        val cardsFile = StudentLovableAccountCardsFile(
            inputStream = pdfInputStream,
            fileName = "test-classroom-students.pdf",
            format = StudentsLovableAccountCardsFormat.PDF
        )

        every {
            studentLovableAccountCardsGenerator.generate(teacher, classroomId, downloadRequest)
        } returns Either.Right(cardsFile)

        val response = controller.getStudentsLovableAccountsForClassroom(teacher, classroomId, downloadRequest)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(MediaType.APPLICATION_PDF.toString(), response.headers.getFirst(HttpHeaders.CONTENT_TYPE))
        assertEquals(
            "attachment; filename=\"test-classroom-students.pdf\"",
            response.headers.getFirst(HttpHeaders.CONTENT_DISPOSITION)
        )
        assertTrue(response.body is InputStreamResource)
        verify { studentLovableAccountCardsGenerator.generate(teacher, classroomId, downloadRequest) }
    }

    @Test
    fun `getStudentsLovableAccountsForClassroom returns CSV file with correct headers on success`() {
        val teacher = testTeacher()
        val classroomId = UUID.randomUUID()
        val downloadRequest = DownloadStudentsLovableAccountsRequest(
            format = StudentsLovableAccountCardsFormat.CSV,
            studentIds = null
        )
        val csvInputStream = ByteArrayInputStream("csv,content".toByteArray())
        val cardsFile = StudentLovableAccountCardsFile(
            inputStream = csvInputStream,
            fileName = "test-classroom-students.csv",
            format = StudentsLovableAccountCardsFormat.CSV
        )

        every {
            studentLovableAccountCardsGenerator.generate(teacher, classroomId, downloadRequest)
        } returns Either.Right(cardsFile)

        val response = controller.getStudentsLovableAccountsForClassroom(teacher, classroomId, downloadRequest)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("text/csv", response.headers.getFirst(HttpHeaders.CONTENT_TYPE))
        assertEquals(
            "attachment; filename=\"test-classroom-students.csv\"",
            response.headers.getFirst(HttpHeaders.CONTENT_DISPOSITION)
        )
        assertTrue(response.body is InputStreamResource)
        verify { studentLovableAccountCardsGenerator.generate(teacher, classroomId, downloadRequest) }
    }

    @Test
    fun `getStudentsLovableAccountsForClassroom handles filtered students request`() {
        val teacher = testTeacher()
        val classroomId = UUID.randomUUID()
        val studentIds = setOf(UUID.randomUUID(), UUID.randomUUID())
        val downloadRequest = DownloadStudentsLovableAccountsRequest(
            format = StudentsLovableAccountCardsFormat.PDF,
            studentIds = studentIds
        )
        val pdfInputStream = ByteArrayInputStream("filtered pdf content".toByteArray())
        val cardsFile = StudentLovableAccountCardsFile(
            inputStream = pdfInputStream,
            fileName = "filtered-students.pdf",
            format = StudentsLovableAccountCardsFormat.PDF
        )

        every {
            studentLovableAccountCardsGenerator.generate(teacher, classroomId, downloadRequest)
        } returns Either.Right(cardsFile)

        val response = controller.getStudentsLovableAccountsForClassroom(teacher, classroomId, downloadRequest)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertTrue(response.body is InputStreamResource)
        verify { studentLovableAccountCardsGenerator.generate(teacher, classroomId, downloadRequest) }
    }

    @Test
    fun `getStudentsLovableAccountsForClassroom maps NotFoundError to 404`() {
        val teacher = testTeacher()
        val classroomId = UUID.randomUUID()
        val downloadRequest = DownloadStudentsLovableAccountsRequest(
            format = StudentsLovableAccountCardsFormat.PDF,
            studentIds = null
        )

        every {
            studentLovableAccountCardsGenerator.generate(teacher, classroomId, downloadRequest)
        } returns Either.Left(NotFoundError("CLASSROOM_NOT_FOUND"))

        val response = controller.getStudentsLovableAccountsForClassroom(teacher, classroomId, downloadRequest)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertNotNull(response.body)
        verify { studentLovableAccountCardsGenerator.generate(teacher, classroomId, downloadRequest) }
    }

    @Test
    fun `getStudentsLovableAccountsForClassroom maps AccessDeniedError to 403`() {
        val teacher = testTeacher()
        val classroomId = UUID.randomUUID()
        val downloadRequest = DownloadStudentsLovableAccountsRequest(
            format = StudentsLovableAccountCardsFormat.PDF,
            studentIds = null
        )

        every {
            studentLovableAccountCardsGenerator.generate(teacher, classroomId, downloadRequest)
        } returns Either.Left(AccessDeniedError("ACCESS_TO_CLASSROOM_DENIED"))

        val response = controller.getStudentsLovableAccountsForClassroom(teacher, classroomId, downloadRequest)

        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
        assertNotNull(response.body)
        verify { studentLovableAccountCardsGenerator.generate(teacher, classroomId, downloadRequest) }
    }

    @Test
    fun `getStudentsLovableAccountsForClassroom maps other errors to appropriate status`() {
        val teacher = testTeacher()
        val classroomId = UUID.randomUUID()
        val downloadRequest = DownloadStudentsLovableAccountsRequest(
            format = StudentsLovableAccountCardsFormat.PDF,
            studentIds = null
        )

        every {
            studentLovableAccountCardsGenerator.generate(teacher, classroomId, downloadRequest)
        } returns Either.Left(MaxNumberOfConnectedAccountsExceededError())

        val response = controller.getStudentsLovableAccountsForClassroom(teacher, classroomId, downloadRequest)

        assertTrue(response.statusCode.isError)
        assertNotNull(response.body)
        verify { studentLovableAccountCardsGenerator.generate(teacher, classroomId, downloadRequest) }
    }

    @Test
    fun `toMediaType extension function returns correct MediaType for PDF`() {
        val controller = LovableController(
            connectLovableAccountToUserUseCase,
            getLovableAccountForUserUseCase,
            getLovableAccountForStudentUseCase,
            reconnectLovableAccountForStudentUseCase,
            enableLovableIntegrationForClassroomUseCase,
            setPausedLovableIntegrationForClassroomUseCase,
            getLovableCredentialsForClassroomUseCase,
            getLovableIntegrationForClassroomUseCase,
            studentLovableAccountCardsGenerator
        )

        // Test the toMediaType extension by triggering it through the controller method
        val teacher = testTeacher()
        val classroomId = UUID.randomUUID()
        val downloadRequest = DownloadStudentsLovableAccountsRequest(
            format = StudentsLovableAccountCardsFormat.PDF,
            studentIds = null
        )
        val pdfInputStream = ByteArrayInputStream("pdf".toByteArray())
        val cardsFile = StudentLovableAccountCardsFile(
            inputStream = pdfInputStream,
            fileName = "test.pdf",
            format = StudentsLovableAccountCardsFormat.PDF
        )

        every {
            studentLovableAccountCardsGenerator.generate(teacher, classroomId, downloadRequest)
        } returns Either.Right(cardsFile)

        val response = controller.getStudentsLovableAccountsForClassroom(teacher, classroomId, downloadRequest)

        assertEquals(MediaType.APPLICATION_PDF.toString(), response.headers.getFirst(HttpHeaders.CONTENT_TYPE))
    }

    @Test
    fun `toMediaType extension function returns correct MediaType for CSV`() {
        val controller = LovableController(
            connectLovableAccountToUserUseCase,
            getLovableAccountForUserUseCase,
            getLovableAccountForStudentUseCase,
            reconnectLovableAccountForStudentUseCase,
            enableLovableIntegrationForClassroomUseCase,
            setPausedLovableIntegrationForClassroomUseCase,
            getLovableCredentialsForClassroomUseCase,
            getLovableIntegrationForClassroomUseCase,
            studentLovableAccountCardsGenerator
        )

        // Test the toMediaType extension by triggering it through the controller method
        val teacher = testTeacher()
        val classroomId = UUID.randomUUID()
        val downloadRequest = DownloadStudentsLovableAccountsRequest(
            format = StudentsLovableAccountCardsFormat.CSV,
            studentIds = null
        )
        val csvInputStream = ByteArrayInputStream("csv".toByteArray())
        val cardsFile = StudentLovableAccountCardsFile(
            inputStream = csvInputStream,
            fileName = "test.csv",
            format = StudentsLovableAccountCardsFormat.CSV
        )

        every {
            studentLovableAccountCardsGenerator.generate(teacher, classroomId, downloadRequest)
        } returns Either.Right(cardsFile)

        val response = controller.getStudentsLovableAccountsForClassroom(teacher, classroomId, downloadRequest)

        assertEquals("text/csv", response.headers.getFirst(HttpHeaders.CONTENT_TYPE))
    }

    @Test
    fun `getLovableAccountForStudent returns 200 with body when found`() {
        val teacher = testTeacher()
        val studentId = UUID.randomUUID()
        val account = LovableAccount(studentId, "s1", "student1", "student1@example.com", "password123")
        every { getLovableAccountForStudentUseCase.get(teacher, studentId) } returns Either.Right(account)

        val response = controller.getLovableAccountForStudent(teacher, studentId)

        assertEquals(200, response.statusCode.value())
        assertTrue(response.statusCode.is2xxSuccessful)
        assertNotNull(response.body)
        verify { getLovableAccountForStudentUseCase.get(teacher, studentId) }
    }

    @Test
    fun `getLovableAccountForStudent maps LOVABLE_ACCOUNT_NOT_FOUND to 404`() {
        val teacher = testTeacher()
        val studentId = UUID.randomUUID()
        every { getLovableAccountForStudentUseCase.get(teacher, studentId) } returns Either.Left(
            NotFoundError("LOVABLE_ACCOUNT_NOT_FOUND")
        )

        val response = controller.getLovableAccountForStudent(teacher, studentId)

        assertEquals(404, response.statusCode.value())
        assertTrue(response.statusCode.isError)
        assertNotNull(response.body)
        verify { getLovableAccountForStudentUseCase.get(teacher, studentId) }
    }

    @Test
    fun `getLovableAccountForStudent maps NotFoundError to 404`() {
        val teacher = testTeacher()
        val studentId = UUID.randomUUID()
        every { getLovableAccountForStudentUseCase.get(teacher, studentId) } returns Either.Left(
            NotFoundError("STUDENT_NOT_FOUND")
        )

        val response = controller.getLovableAccountForStudent(teacher, studentId)

        assertEquals(404, response.statusCode.value())
        assertTrue(response.statusCode.isError)
        assertNotNull(response.body)
        verify { getLovableAccountForStudentUseCase.get(teacher, studentId) }
    }

    @Test
    fun `getLovableAccountForStudent maps AccessDeniedError to 403`() {
        val teacher = testTeacher()
        val studentId = UUID.randomUUID()
        every { getLovableAccountForStudentUseCase.get(teacher, studentId) } returns Either.Left(
            AccessDeniedError("ACCESS_TO_CLASSROOM_DENIED")
        )

        val response = controller.getLovableAccountForStudent(teacher, studentId)

        assertEquals(403, response.statusCode.value())
        assertTrue(response.statusCode.isError)
        assertNotNull(response.body)
        verify { getLovableAccountForStudentUseCase.get(teacher, studentId) }
    }

    @Test
    fun `reconnectLovableAccountForStudent returns 200 with body when successful`() {
        val teacher = testTeacher()
        val studentId = UUID.randomUUID()
        val account = LovableAccount(studentId, "s1", "student1", "student1@example.com", "newpassword123")
        every {
            reconnectLovableAccountForStudentUseCase.reconnect(
                teacher,
                studentId,
                classroomId
            )
        } returns Either.Right(account)

        val response = controller.reconnectLovableAccountForStudent(teacher, studentId, classroomId)

        assertEquals(200, response.statusCode.value())
        assertTrue(response.statusCode.is2xxSuccessful)
        assertNotNull(response.body)
        verify { reconnectLovableAccountForStudentUseCase.reconnect(teacher, studentId, classroomId) }
    }

    @Test
    fun `reconnectLovableAccountForStudent maps NotFoundError to 404`() {
        val teacher = testTeacher()
        val studentId = UUID.randomUUID()
        every {
            reconnectLovableAccountForStudentUseCase.reconnect(
                teacher,
                studentId,
                classroomId
            )
        } returns Either.Left(
            NotFoundError("STUDENT_NOT_FOUND")
        )

        val response = controller.reconnectLovableAccountForStudent(teacher, studentId, classroomId)

        assertEquals(404, response.statusCode.value())
        assertTrue(response.statusCode.isError)
        assertNotNull(response.body)
        verify { reconnectLovableAccountForStudentUseCase.reconnect(teacher, studentId, classroomId) }
    }

    @Test
    fun `reconnectLovableAccountForStudent maps AccessDeniedError to 403`() {
        val teacher = testTeacher()
        val studentId = UUID.randomUUID()
        every {
            reconnectLovableAccountForStudentUseCase.reconnect(
                teacher,
                studentId,
                classroomId
            )
        } returns Either.Left(
            AccessDeniedError("ACCESS_TO_CLASSROOM_DENIED")
        )

        val response = controller.reconnectLovableAccountForStudent(teacher, studentId, classroomId)

        assertEquals(403, response.statusCode.value())
        assertTrue(response.statusCode.isError)
        assertNotNull(response.body)
        verify { reconnectLovableAccountForStudentUseCase.reconnect(teacher, studentId, classroomId) }
    }

    @Test
    fun `reconnectLovableAccountForStudent maps OutOfLovableAccountsError to appropriate status`() {
        val teacher = testTeacher()
        val studentId = UUID.randomUUID()
        every {
            reconnectLovableAccountForStudentUseCase.reconnect(
                teacher,
                studentId,
                classroomId
            )
        } returns Either.Left(
            OutOfLovableAccountsError()
        )

        val response = controller.reconnectLovableAccountForStudent(teacher, studentId, classroomId)

        assertTrue(response.statusCode.isError)
        assertNotNull(response.body)
        verify { reconnectLovableAccountForStudentUseCase.reconnect(teacher, studentId, classroomId) }
    }

    @Test
    fun `reconnectLovableAccountForStudent maps MaxNumberOfConnectedAccountsExceededError to appropriate status`() {
        val teacher = testTeacher()
        val studentId = UUID.randomUUID()
        every {
            reconnectLovableAccountForStudentUseCase.reconnect(
                teacher,
                studentId,
                classroomId
            )
        } returns Either.Left(
            MaxNumberOfConnectedAccountsExceededError()
        )

        val response = controller.reconnectLovableAccountForStudent(teacher, studentId, classroomId)

        assertTrue(response.statusCode.isError)
        assertNotNull(response.body)
        verify { reconnectLovableAccountForStudentUseCase.reconnect(teacher, studentId, classroomId) }
    }
}
