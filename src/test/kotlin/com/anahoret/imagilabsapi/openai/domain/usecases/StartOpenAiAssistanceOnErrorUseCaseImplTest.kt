package com.anahoret.imagilabsapi.openai.domain.usecases

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.openai.domain.OpenAiAssistance
import com.anahoret.imagilabsapi.openai.domain.OpenAiAssistanceService
import com.anahoret.imagilabsapi.openai.domain.OpenAiPrompts
import com.anahoret.imagilabsapi.openai.domain.OpenAiService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("Start open AI assistance on error use case")
class StartOpenAiAssistanceOnErrorUseCaseImplTest {

    private val openAiRequestValidator = mockk<OpenAiRequestValidator>()
    private val openAiService = mockk<OpenAiService>()
    private val openAiAssistanceService = mockk<OpenAiAssistanceService>()
    private val getOpenAiAssistanceOnErrorUseCase =
        StartOpenAiAssistanceOnErrorUseCaseImpl(openAiRequestValidator, openAiService, openAiAssistanceService)

    private val userId = UUID.randomUUID()
    private val user = mockk<UserProfile> {
        every { id } returns userId
    }
    private val projectId = UUID.randomUUID()
    private val sessionId = UUID.randomUUID()
    private val request = ErrorAssistanceRequest(sessionId, projectId, "User code", "Error message")

    @Test
    fun `should return error when AI request is invalid`() {
        val error = mockk<OperationError>()
        every { openAiRequestValidator.validate(user, request) } returns error.left()
        when (val result = getOpenAiAssistanceOnErrorUseCase.get(user, request)) {
            is Either.Left -> assertTrue(result.value is OperationError)
            is Either.Right -> fail()
        }
    }

    @Test
    fun `should generate second directive with error`() {
        val assistanceId = UUID.randomUUID()
        val secondDirectiveWithError = "${OpenAiPrompts.SECOND_DIRECTIVE} I am receiving this error: Error message"
        every { openAiRequestValidator.validate(user, request) } returns Unit.right()
        every { openAiService.startAssistance("User code", secondDirectiveWithError) } returns mockk {
            every { results } returns listOf(mockk {
                every { output } returns mockk {
                    every { content } returns "Your code is incorrect!"
                }
            })
        }
        every { openAiAssistanceService.existsBySessionId(sessionId) } returns false
        every {
            openAiAssistanceService.save(
                userId,
                "I am receiving this error: Error message",
                "Your code is incorrect!",
                request
            )
        } returns OpenAiAssistance(assistanceId, userId)
        when (getOpenAiAssistanceOnErrorUseCase.get(user, request)) {
            is Either.Left -> fail()
            is Either.Right -> verify { openAiService.startAssistance("User code", secondDirectiveWithError) }
        }
    }

    @Test
    fun `should return content`() {
        val assistanceId = UUID.randomUUID()
        val secondDirectiveWithError = "${OpenAiPrompts.SECOND_DIRECTIVE} I am receiving this error: Error message"
        every { openAiRequestValidator.validate(user, request) } returns Unit.right()
        every { openAiService.startAssistance("User code", secondDirectiveWithError) } returns mockk {
            every { results } returns listOf(mockk {
                every { output } returns mockk {
                    every { content } returns "Your code is incorrect!"
                }
            })
        }
        every { openAiAssistanceService.existsBySessionId(sessionId) } returns false
        every {
            openAiAssistanceService.save(
                userId,
                "I am receiving this error: Error message",
                "Your code is incorrect!",
                request
            )
        } returns OpenAiAssistance(assistanceId, userId)
        when (val res = getOpenAiAssistanceOnErrorUseCase.get(user, request)) {
            is Either.Left -> fail()
            is Either.Right -> assertAll(
                { assertEquals("Your code is incorrect!", res.value.aiResponse) },
                { assertEquals(assistanceId, res.value.assistanceId) }
            )
        }
    }

}
