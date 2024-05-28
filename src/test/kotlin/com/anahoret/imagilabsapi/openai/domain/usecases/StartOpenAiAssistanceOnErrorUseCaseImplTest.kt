package com.anahoret.imagilabsapi.openai.domain.usecases

import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.openai.domain.*
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
    private val openAiPreconditionChecker = mockk<OpenAiPreconditionChecker>()
    private val tipTokensService = mockk<TipTokensService>()
    private val getOpenAiAssistanceOnErrorUseCase =
        StartOpenAiAssistanceOnErrorUseCaseImpl(
            openAiRequestValidator,
            openAiService,
            openAiAssistanceService,
            openAiPreconditionChecker,
            tipTokensService
        )

    private val userId = UUID.randomUUID()
    private val user = mockk<UserProfile> {
        every { id } returns userId
    }
    private val projectId = UUID.randomUUID()
    private val sessionId = UUID.randomUUID()
    private val request = ErrorAssistanceRequest(sessionId, projectId, "User code", "Error message")

    @Test
    fun `should return error when AI request is invalid`() {
        val error = mockk<ValidationError>()
        every { openAiRequestValidator.validate(request, user) } returns error.left()
        getOpenAiAssistanceOnErrorUseCase.getAssistance(request, user).fold(
            { assertTrue(it is ValidationError) }, { fail() }
        )
    }

    @Test
    fun `should return error when preconditions not checked`() {
        val error = mockk<NotFoundError>()
        every { openAiRequestValidator.validate(request, user) } returns Unit.right()
        every { openAiPreconditionChecker.check(request, user) } returns error.left()
        getOpenAiAssistanceOnErrorUseCase.getAssistance(request, user).fold(
            { assertTrue(it is NotFoundError) }, { fail() }
        )
    }

    @Test
    fun `should generate second directive with error`() {
        val assistanceId = UUID.randomUUID()
        val secondDirectiveWithError = "${OpenAiPrompts.SECOND_DIRECTIVE} I am receiving this error: Error message"
        every { openAiRequestValidator.validate(request, user) } returns Unit.right()
        every { openAiPreconditionChecker.check(request, user) } returns Unit.right()
        every { openAiService.startAssistance("User code", secondDirectiveWithError) } returns "Your code is incorrect!"
        every { openAiAssistanceService.existsBySessionId(sessionId) } returns false
        every {
            openAiAssistanceService.save(
                userId,
                "I am receiving this error: Error message",
                "Your code is incorrect!",
                request
            )
        } returns OpenAiAssistance(assistanceId, userId)
        every { tipTokensService.withdrawOneTipToken(userId) } returns Unit
        getOpenAiAssistanceOnErrorUseCase.getAssistance(request, user).fold(
            { fail() }, { verify { openAiService.startAssistance("User code", secondDirectiveWithError) } }
        )
    }

    @Test
    fun `should return content`() {
        val assistanceId = UUID.randomUUID()
        val secondDirectiveWithError = "${OpenAiPrompts.SECOND_DIRECTIVE} I am receiving this error: Error message"
        every { openAiRequestValidator.validate(request, user) } returns Unit.right()
        every { openAiPreconditionChecker.check(request, user) } returns Unit.right()
        every { openAiService.startAssistance("User code", secondDirectiveWithError) } returns "Your code is incorrect!"
        every { openAiAssistanceService.existsBySessionId(sessionId) } returns false
        every {
            openAiAssistanceService.save(
                userId,
                "I am receiving this error: Error message",
                "Your code is incorrect!",
                request
            )
        } returns OpenAiAssistance(assistanceId, userId)
        every { tipTokensService.withdrawOneTipToken(userId) } returns Unit
        getOpenAiAssistanceOnErrorUseCase.getAssistance(request, user).fold(
            { fail() }, {
                assertAll(
                    { assertEquals("Your code is incorrect!", it.aiResponse) },
                    { assertEquals(assistanceId, it.assistanceId) }
                )
            }
        )
    }

    @Test
    fun `should withdraw tip token`() {
        val assistanceId = UUID.randomUUID()
        val secondDirectiveWithError = "${OpenAiPrompts.SECOND_DIRECTIVE} I am receiving this error: Error message"
        every { openAiRequestValidator.validate(request, user) } returns Unit.right()
        every { openAiPreconditionChecker.check(request, user) } returns Unit.right()
        every { openAiService.startAssistance("User code", secondDirectiveWithError) } returns "Your code is incorrect!"
        every { openAiAssistanceService.existsBySessionId(sessionId) } returns false
        every {
            openAiAssistanceService.save(
                userId,
                "I am receiving this error: Error message",
                "Your code is incorrect!",
                request
            )
        } returns OpenAiAssistance(assistanceId, userId)
        every { tipTokensService.withdrawOneTipToken(userId) } returns Unit
        getOpenAiAssistanceOnErrorUseCase.getAssistance(request, user)
            .fold({ fail() }, { verify { tipTokensService.withdrawOneTipToken(userId) } })
    }
}
