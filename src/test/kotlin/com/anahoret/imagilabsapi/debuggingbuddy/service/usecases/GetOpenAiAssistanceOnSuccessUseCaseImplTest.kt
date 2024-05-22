package com.anahoret.imagilabsapi.debuggingbuddy.service.usecases

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.openai.domain.OpenAiAssistance
import com.anahoret.imagilabsapi.openai.domain.OpenAiAssistanceService
import com.anahoret.imagilabsapi.openai.domain.OpenAiPrompts
import com.anahoret.imagilabsapi.openai.domain.OpenAiService
import com.anahoret.imagilabsapi.openai.domain.usecases.GetOpenAiAssistanceOnSuccessUseCaseImpl
import com.anahoret.imagilabsapi.openai.domain.usecases.OpenAiRequestValidator
import com.anahoret.imagilabsapi.openai.domain.usecases.QuestionAssistanceRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("Get Open Ai assistance on success use case")
class GetOpenAiAssistanceOnSuccessUseCaseImplTest {

    private val openAiService = mockk<OpenAiService>()
    private val openAiAssistanceService = mockk<OpenAiAssistanceService>()
    private val openAiRequestValidator = mockk<OpenAiRequestValidator>()
    private val getOpenAiAssistanceOnSuccessUseCase = GetOpenAiAssistanceOnSuccessUseCaseImpl(
        openAiService,
        openAiAssistanceService,
        openAiRequestValidator
    )

    private val userId = UUID.randomUUID()
    private val user = mockk<UserProfile> {
        every { id } returns userId
    }
    private val projectId = UUID.randomUUID()
    private val sessionId = UUID.randomUUID()
    private val request = QuestionAssistanceRequest(sessionId, projectId, "User code", "What is 'm' in my code?")

    @Test
    fun `should return error when AI request is invalid`() {
        val error = mockk<OperationError>()
        every { openAiRequestValidator.validate(user, request) } returns error.left()
        when (val result = getOpenAiAssistanceOnSuccessUseCase.get(request, user)) {
            is Either.Left -> assertTrue(result.value is OperationError)
            is Either.Right -> fail()
        }
    }

    @Test
    fun `should generate second directive with question`() {
        val assistanceId = UUID.randomUUID()
        val secondDirectiveWithQuestion = "My question is: What is 'm' in my code? ${OpenAiPrompts.SECOND_DIRECTIVE}"
        every { openAiRequestValidator.validate(user, request) } returns Unit.right()
        every { openAiService.startAssistance("User code", secondDirectiveWithQuestion) } returns "Great result!"
        every { openAiAssistanceService.existsBySessionId(sessionId) } returns false
        every {
            openAiAssistanceService.save(
                userId,
                "My question is: What is 'm' in my code?",
                "Great result!",
                request
            )
        } returns OpenAiAssistance(assistanceId, userId)
        when (getOpenAiAssistanceOnSuccessUseCase.get(request, user)) {
            is Either.Left -> fail()
            is Either.Right -> verify { openAiService.startAssistance("User code", secondDirectiveWithQuestion) }
        }
    }

    @Test
    fun `should return content`() {
        val assistanceId = UUID.randomUUID()
        val secondDirectiveWithQuestion = "My question is: What is 'm' in my code? ${OpenAiPrompts.SECOND_DIRECTIVE}"
        every { openAiRequestValidator.validate(user, request) } returns Unit.right()
        every { openAiService.startAssistance("User code", secondDirectiveWithQuestion) } returns "Great result!"
        every { openAiAssistanceService.existsBySessionId(sessionId) } returns false
        every {
            openAiAssistanceService.save(
                userId,
                "My question is: What is 'm' in my code?",
                "Great result!",
                request
            )
        } returns OpenAiAssistance(assistanceId, userId)
        when (val res = getOpenAiAssistanceOnSuccessUseCase.get(request, user)) {
            is Either.Left -> fail()
            is Either.Right -> assertAll(
                { assertEquals("Great result!", res.value.aiResponse) },
                { assertEquals(assistanceId, res.value.assistanceId) }
            )
        }
    }
}
