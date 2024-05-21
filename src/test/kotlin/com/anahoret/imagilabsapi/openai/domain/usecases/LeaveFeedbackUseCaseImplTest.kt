package com.anahoret.imagilabsapi.openai.domain.usecases

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.openai.domain.OpenAiAssistanceService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("Leave feedback use case")
class LeaveFeedbackUseCaseImplTest {

    private val openAiAssistanceService = mockk<OpenAiAssistanceService>()
    private val leaveFeedbackUseCase = LeaveFeedbackUseCaseImpl(openAiAssistanceService)

    private val assistanceId = UUID.randomUUID()
    private val isHelpful = true
    private val testUserId = UUID.randomUUID()
    private val userProfile = mockk<UserProfile> {
        every { id } returns testUserId
    }

    @Test
    fun `should return error when assistance not exists`() {
        every { openAiAssistanceService.getById(assistanceId) } returns null
        when (val result = leaveFeedbackUseCase.leaveFeedback(assistanceId, isHelpful, userProfile)) {
            is Either.Left -> assertAll(
                { assertTrue(result.value is NotFoundError) },
                { assertEquals("ASSISTANCE_NOT_FOUND", (result.value as NotFoundError).message) }
            )

            is Either.Right -> fail()
        }
    }

    @Test
    fun `should return error when user has no access to leave feedback`() {
        every { openAiAssistanceService.getById(assistanceId) } returns mockk {
            every { userId } returns UUID.randomUUID()
        }
        when (val result = leaveFeedbackUseCase.leaveFeedback(assistanceId, isHelpful, userProfile)) {
            is Either.Left -> assertAll(
                { assertTrue(result.value is AccessDeniedError) },
                { assertEquals("NO_ACCESS_TO_LEAVE_FEEDBACK", (result.value as AccessDeniedError).message) }
            )

            is Either.Right -> fail()
        }
    }

    @Test
    fun `should leave feedback when user has access`() {
        every { openAiAssistanceService.getById(assistanceId) } returns mockk {
            every { userId } returns testUserId
        }
        every { openAiAssistanceService.leaveFeedback(assistanceId, isHelpful) } returns Unit
        when (leaveFeedbackUseCase.leaveFeedback(assistanceId, isHelpful, userProfile)) {
            is Either.Left -> fail()
            is Either.Right -> verify { openAiAssistanceService.leaveFeedback(assistanceId, isHelpful) }
        }
    }
}
