package com.anahoret.imagilabsapi.lovable.web

import arrow.core.Either
import com.anahoret.imagilabsapi.common.testTeacher
import com.anahoret.imagilabsapi.lovable.domain.*
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.http.ResponseEntity
import java.util.*

class LovableControllerTest {

    private val connectLovableAccountToUserUseCase: ConnectLovableAccountToUserUseCase = mockk()
    private val getLovableAccountForUserUseCase: GetLovableAccountForUserUseCase = mockk()
    private val enableLovableIntegrationForClassroomUseCase: EnableLovableIntegrationForClassroomUseCase = mockk()
    private val setPausedLovableIntegrationForClassroomUseCase: SetPausedLovableIntegrationForClassroomUseCase = mockk()
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
    fun `getLovableAccount returns 200 with body when found`() {
        val teacher = testTeacher()
        val account = LovableAccount(UUID.randomUUID(), "b@x.com", "pwd")
        every { getLovableAccountForUserUseCase.get(teacher) } returns account

        val response: ResponseEntity<*> = controller.getLovableAccount(teacher)

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
}
