package com.anahoret.imagilabsapi.openai.domain.usecases

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.openai.domain.OpenAiAssistanceService
import com.anahoret.imagilabsapi.openai.domain.OpenAiService
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
    private val proceedOpenAiAssistanceOnErrorUseCase =
        ProceedOpenAiAssistanceOnErrorUseCaseImpl(openAiRequestValidator, openAiAssistanceService, openAiService)


    private val userProfile = mockk<UserProfile>()
    private val request = ProceedAssistanceRequest(UUID.randomUUID(), UUID.randomUUID(), "Input")

    @Test
    fun `should return error when request isn't valid`() {
        val error = mockk<ValidationError>()
        every { openAiRequestValidator.validate(userProfile, request) } returns error.left()
        when (val res = proceedOpenAiAssistanceOnErrorUseCase.getAssistance(userProfile, request)) {
            is Either.Left -> assertTrue(res.value is ValidationError)
            is Either.Right -> fail()
        }
    }

    @Test
    fun `should return AI response`() {
        val assistanceId = UUID.randomUUID()
        val allAssistance = listOf(mockk<OpenAiAssistanceContent>())
        every { openAiRequestValidator.validate(userProfile, request) } returns Unit.right()
        every { openAiAssistanceService.getAllBySessionId(request.sessionId) } returns allAssistance
        every { openAiService.proceedAssistanceOnError(request.input, allAssistance) } returns "response"
        every { openAiAssistanceService.save(userProfile.id, request.input, "response", request) } returns mockk {
            every { id } returns assistanceId
        }
        when (val res = proceedOpenAiAssistanceOnErrorUseCase.getAssistance(userProfile, request)) {
            is Either.Left -> fail()
            is Either.Right -> assertAll(
                { assertEquals("response", res.value.aiResponse) },
                { assertEquals(assistanceId, res.value.assistanceId) }
            )
        }
    }
}
