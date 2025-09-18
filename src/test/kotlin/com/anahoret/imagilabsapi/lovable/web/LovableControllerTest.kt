package com.anahoret.imagilabsapi.lovable.web

import arrow.core.Either
import com.anahoret.imagilabsapi.common.testStudent
import com.anahoret.imagilabsapi.common.testTeacher
import com.anahoret.imagilabsapi.lovable.domain.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.http.ResponseEntity
import java.util.*

class LovableControllerTest {

    private val connectLovableAccountToUserUseCase: ConnectLovableAccountToUserUseCase = mockk()
    private val getLovableAccountForUserUseCase: GetLovableAccountForUserUseCase = mockk()
    private val enableLovableIntegrationForClassroomUseCase: EnableLovableIntegrationForClassroomUseCase = mockk()
    private val setPausedLovableIntegrationForClassroomUseCase: SetPausedLovableIntegrationForClassroomUseCase =
        mockk(relaxed = true)
    private val getLovableCredentialsForClassroomUseCase: GetLovableCredentialsForClassroomUseCase = mockk()
    private val controller =
        LovableController(
            connectLovableAccountToUserUseCase,
            getLovableAccountForUserUseCase,
            enableLovableIntegrationForClassroomUseCase,
            setPausedLovableIntegrationForClassroomUseCase,
            getLovableCredentialsForClassroomUseCase
        )

    @Test
    fun `connectTeacherProfile returns 200 with body on success`() {
        val teacher = testTeacher()
        val account = LovableAccount(UUID.randomUUID(), "a@x.com", "p")
        every { connectLovableAccountToUserUseCase.connect(teacher) } returns Either.Right(account)

        val response: ResponseEntity<*> = controller.connectTeacherProfile(teacher)

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

        val response: ResponseEntity<*> = controller.connectTeacherProfile(teacher)

        assertTrue(!response.statusCode.is2xxSuccessful)
        assertTrue(response.statusCode.isError)
        assertNotNull(response.body)
    }

    @Test
    fun `getLovableAccount returns 200 with body when found for teacher`() {
        val teacher = testTeacher()
        val account = LovableAccount(UUID.randomUUID(), "b@x.com", "pwd")
        every { getLovableAccountForUserUseCase.get(teacher) } returns account

        val response: ResponseEntity<*> = controller.getLovableAccount(teacher)

        assertEquals(200, response.statusCode.value())
        assertTrue(response.statusCode.is2xxSuccessful)
        assertNotNull(response.body)
    }

    @Test
    fun `getLovableAccount returns 200 with body when found for student`() {
        val student = testStudent()
        val account = LovableAccount(UUID.randomUUID(), "b@x.com", "pwd")
        every { getLovableAccountForUserUseCase.get(student) } returns account

        val response: ResponseEntity<*> = controller.getLovableAccount(student)

        assertEquals(200, response.statusCode.value())
        assertTrue(response.statusCode.is2xxSuccessful)
        assertNotNull(response.body)
    }

    @Test
    fun `getLovableAccount returns 404 when not found`() {
        val teacher = testTeacher()
        every { getLovableAccountForUserUseCase.get(teacher) } returns null

        val response: ResponseEntity<*> = controller.getLovableAccount(teacher)

        assertEquals(404, response.statusCode.value())
        assertTrue(response.body == null)
    }

    @Test
    fun `enableIntegrationForClassroom returns 200 on success`() {
        val teacher = testTeacher()
        val classroomId = UUID.randomUUID()
        every { enableLovableIntegrationForClassroomUseCase.enable(teacher, classroomId) } returns Either.Right(Unit)

        val response: ResponseEntity<*> = controller.enableIntegrationForClassroom(teacher, classroomId)

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

        val response: ResponseEntity<*> = controller.enableIntegrationForClassroom(teacher, classroomId)

        assertTrue(response.statusCode.isError)
        assertNotEquals(200, response.statusCode.value())
        assertNotNull(response.body)
    }

    @Test
    fun `setPausedIntegrationForClassroom returns 200 and passes args`() {
        val teacher = testTeacher()
        val classroomId = UUID.randomUUID()
        val request = LovableController.SetPausedRequest(true)

        val response: ResponseEntity<Void> = controller.setPausedIntegrationForClassroom(teacher, classroomId, request)

        assertEquals(200, response.statusCode.value())
        verify { setPausedLovableIntegrationForClassroomUseCase.setPaused(teacher, classroomId, true) }
    }

    @Test
    fun `getStudentsCredentialsForClassroom returns 200 with body on success`() {
        val teacher = testTeacher()
        val classroomId = UUID.randomUUID()
        val list = listOf(LovableAccount(UUID.randomUUID(), "c@x.com", "pass"))
        every { getLovableCredentialsForClassroomUseCase.getCredentials(teacher, classroomId) } returns Either.Right(
            list
        )

        val response: ResponseEntity<*> = controller.getStudentsCredentialsForClassroom(teacher, classroomId)

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

        val response: ResponseEntity<*> = controller.getStudentsCredentialsForClassroom(teacher, classroomId)

        assertTrue(response.statusCode.isError)
        assertNotNull(response.body)
    }
}
