package com.anahoret.imagilabsapi.debuggingbuddy.service.usecases

import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.openai.domain.*
import com.anahoret.imagilabsapi.openai.domain.usecases.GetOpenAiAssistanceOnSuccessUseCaseImpl
import com.anahoret.imagilabsapi.openai.domain.usecases.OpenAiPreconditionChecker
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
    private val openAiPreconditionChecker = mockk<OpenAiPreconditionChecker>()
    private val tipTokensService = mockk<TipTokensService>()
    private val getOpenAiAssistanceOnSuccessUseCase = GetOpenAiAssistanceOnSuccessUseCaseImpl(
        openAiService,
        openAiAssistanceService,
        openAiRequestValidator,
        openAiPreconditionChecker,
        tipTokensService
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
        val error = mockk<ValidationError>()
        every { openAiRequestValidator.validate(request, user) } returns error.left()
        getOpenAiAssistanceOnSuccessUseCase.get(request, user).fold(
            { assertTrue(it is ValidationError) },
            { fail() }
        )
    }

    @Test
    fun `should return error when preconditions not checked`() {
        val error = mockk<NotFoundError>()
        every { openAiRequestValidator.validate(request, user) } returns Unit.right()
        every { openAiPreconditionChecker.check(request, user) } returns error.left()
        getOpenAiAssistanceOnSuccessUseCase.get(request, user).fold(
            { assertTrue(it is NotFoundError) },
            { fail() }
        )
    }

    @Test
    fun `should generate second directive with question`() {
        val assistanceId = UUID.randomUUID()
        val secondDirectiveWithQuestion = "My question is: What is 'm' in my code? ${OpenAiPrompts.SECOND_DIRECTIVE}"
        every { openAiRequestValidator.validate(request, user) } returns Unit.right()
        every { openAiPreconditionChecker.check(request, user) } returns Unit.right()
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
        every { tipTokensService.withdrawOneTipToken(userId) } returns Unit
        getOpenAiAssistanceOnSuccessUseCase.get(request, user).fold(
            { fail() },
            { verify { openAiService.startAssistance("User code", secondDirectiveWithQuestion) } }
        )
    }

    @Test
    fun `should return content`() {
        val assistanceId = UUID.randomUUID()
        val secondDirectiveWithQuestion = "My question is: What is 'm' in my code? ${OpenAiPrompts.SECOND_DIRECTIVE}"
        every { openAiRequestValidator.validate(request, user) } returns Unit.right()
        every { openAiPreconditionChecker.check(request, user) } returns Unit.right()
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
        every { tipTokensService.withdrawOneTipToken(userId) } returns Unit
        getOpenAiAssistanceOnSuccessUseCase.get(request, user).fold(
            { fail() },
            {
                assertAll(
                    { assertEquals("Great result!", it.aiResponse) },
                    { assertEquals(assistanceId, it.assistanceId) }
                )
            }
        )
    }

    @Test
    fun `should withdraw one tip token when user get assistance`() {
        val assistanceId = UUID.randomUUID()
        val secondDirectiveWithQuestion = "My question is: What is 'm' in my code? ${OpenAiPrompts.SECOND_DIRECTIVE}"
        every { openAiRequestValidator.validate(request, user) } returns Unit.right()
        every { openAiPreconditionChecker.check(request, user) } returns Unit.right()
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
        every { tipTokensService.withdrawOneTipToken(userId) } returns Unit
        getOpenAiAssistanceOnSuccessUseCase.get(request, user)
            .fold({ fail() }, { verify { tipTokensService.withdrawOneTipToken(userId) } })
    }
}
