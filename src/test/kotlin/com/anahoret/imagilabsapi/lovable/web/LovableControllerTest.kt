package com.anahoret.imagilabsapi.lovable.web

import arrow.core.Either
import com.anahoret.imagilabsapi.common.testTeacher
import com.anahoret.imagilabsapi.lovable.domain.ConnectLovableAccountToTeacherUseCase
import com.anahoret.imagilabsapi.lovable.domain.GetLovableAccountForTeacherUseCase
import com.anahoret.imagilabsapi.lovable.domain.LovableAccount
import com.anahoret.imagilabsapi.lovable.domain.MaxNumberOfConnectedAccountsExceededError
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.http.ResponseEntity

class LovableControllerTest {

    private val connectLovableAccountToTeacherUseCase: ConnectLovableAccountToTeacherUseCase = mockk()
    private val getLovableAccountForTeacherUseCase: GetLovableAccountForTeacherUseCase = mockk()
    private val controller =
        LovableController(connectLovableAccountToTeacherUseCase, getLovableAccountForTeacherUseCase)

    @Test
    fun `connectTeacherProfile returns 200 with body on success`() {
        val teacher = testTeacher()
        val account = LovableAccount("a@x.com", "p")
        every { connectLovableAccountToTeacherUseCase.connect(teacher) } returns Either.Right(account)

        val response: ResponseEntity<*> = controller.connectTeacherProfile(teacher)

        assertTrue(response.statusCode.is2xxSuccessful)
        assertEquals(200, response.statusCode.value())
        val body = response.body
        assertNotNull(body)
    }

    @Test
    fun `connectTeacherProfile maps errors to non-2xx`() {
        val teacher = testTeacher()
        every { connectLovableAccountToTeacherUseCase.connect(teacher) } returns Either.Left(
            MaxNumberOfConnectedAccountsExceededError()
        )

        val response: ResponseEntity<*> = controller.connectTeacherProfile(teacher)

        assertTrue(!response.statusCode.is2xxSuccessful)
        assertTrue(response.statusCode.isError)
        assertNotNull(response.body)
    }
}
