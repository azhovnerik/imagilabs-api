package com.anahoret.imagilabsapi.openai.domain.usecases

import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.openai.domain.OpenAiAssistanceService
import com.anahoret.imagilabsapi.openai.domain.OpenAiService
import com.anahoret.imagilabsapi.openai.domain.TipTokensService
import com.anahoret.imagilabsapi.openai.storage.OpenAiAssistanceContent
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.util.*

@DisplayName("Proceed open Ai assistance on error use case")
class ProceedOpenAiAssistanceOnErrorUseCaseImplTest {

    private val openAiRequestValidator = mockk<OpenAiRequestValidator>()
    private val openAiAssistanceService = mockk<OpenAiAssistanceService>()
    private val openAiService = mockk<OpenAiService>()
    private val openAiPreconditionChecker = mockk<OpenAiPreconditionChecker>()
    private val tipTokensService = mockk<TipTokensService>()
    private val proceedOpenAiAssistanceOnErrorUseCase =
        ProceedOpenAiAssistanceOnErrorUseCaseImpl(
            openAiRequestValidator,
            openAiAssistanceService,
            openAiService,
            openAiPreconditionChecker,
            tipTokensService
        )

    private val userProfile = mockk<UserProfile>()
    private val request = ProceedAssistanceRequest(UUID.randomUUID(), UUID.randomUUID(), "Input")

    @Test
    fun `should return error when request isn't valid`() {
        val error = mockk<ValidationError>()
        every { openAiRequestValidator.validate(request, userProfile) } returns error.left()
        proceedOpenAiAssistanceOnErrorUseCase.getAssistance(request, userProfile).fold(
            { assertTrue(it is ValidationError) },
            { fail() }
        )
    }

    @Test
    fun `should return error when precondiotions not checked`() {
        val error = mockk<NotFoundError>()
        every { openAiRequestValidator.validate(request, userProfile) } returns Unit.right()
        every { openAiPreconditionChecker.check(request, userProfile) } returns error.left()
        proceedOpenAiAssistanceOnErrorUseCase.getAssistance(request, userProfile).fold(
            { assertTrue(it is NotFoundError) },
            { fail() }
        )
    }

    @Test
    fun `should return AI response`() {
        val assistanceId = UUID.randomUUID()
        val allAssistance = listOf(mockk<OpenAiAssistanceContent>())
        every { openAiRequestValidator.validate(request, userProfile) } returns Unit.right()
        every { openAiPreconditionChecker.check(request, userProfile) } returns Unit.right()
        every { openAiAssistanceService.getAllBySessionId(request.sessionId) } returns allAssistance
        every { openAiService.proceedAssistanceOnError(request.input, allAssistance) } returns "response"
        every { openAiAssistanceService.save(userProfile.id, request.input, "response", request) } returns mockk {
            every { id } returns assistanceId
        }
        every { tipTokensService.withdrawOneTipToken(userProfile.id) } returns Unit
        proceedOpenAiAssistanceOnErrorUseCase.getAssistance(request, userProfile).fold(
            { fail() },
            {
                assertAll(
                    { assertEquals("response", it.aiResponse) },
                    { assertEquals(assistanceId, it.assistanceId) }
                )
            }
        )
    }

    @Test
    fun `should withdraw tip tokens`() {
        val assistanceId = UUID.randomUUID()
        val allAssistance = listOf(mockk<OpenAiAssistanceContent>())
        every { openAiRequestValidator.validate(request, userProfile) } returns Unit.right()
        every { openAiPreconditionChecker.check(request, userProfile) } returns Unit.right()
        every { openAiAssistanceService.getAllBySessionId(request.sessionId) } returns allAssistance
        every { openAiService.proceedAssistanceOnError(request.input, allAssistance) } returns "response"
        every { openAiAssistanceService.save(userProfile.id, request.input, "response", request) } returns mockk {
            every { id } returns assistanceId
        }
        every { tipTokensService.withdrawOneTipToken(userProfile.id) } returns Unit
        proceedOpenAiAssistanceOnErrorUseCase.getAssistance(request, userProfile)
            .fold({ fail() }, { tipTokensService.withdrawOneTipToken(userProfile.id) })
    }
}
