package com.anahoret.imagilabsapi.openai.domain.usecases

import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.openai.domain.*
import com.anahoret.imagilabsapi.users.UserType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.ai.openai.api.OpenAiApi
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
    private val userProfile = mockk<UserProfile> {
        every { id } returns userId
        every { userType } returns UserType.STUDENT
    }
    private val sessionId = UUID.randomUUID()
    private val request = ErrorAssistanceRequest(sessionId, "User code", "Error message")
    private val chatOptions: OpenAiChatOptions = OpenAiChatOptions()
        .apply { responseFormat = OpenAiApi.ChatCompletionRequest.ResponseFormat("json_object") }

    @Test
    fun `should return error when AI request is invalid`() {
        val error = mockk<ValidationError>()
        every { openAiRequestValidator.validate(request, userProfile) } returns error.left()
        getOpenAiAssistanceOnErrorUseCase.getAssistance(request, userProfile).fold(
            { assertTrue(it is ValidationError) }, { fail() }
        )
    }

    @Test
    fun `should return error when preconditions not checked`() {
        val error = mockk<NotFoundError>()
        every { openAiRequestValidator.validate(request, userProfile) } returns Unit.right()
        every { openAiPreconditionChecker.check(request, userProfile) } returns error.left()
        getOpenAiAssistanceOnErrorUseCase.getAssistance(request, userProfile).fold(
            { assertTrue(it is NotFoundError) }, { fail() }
        )
    }

    @Test
    fun `should generate second directive with error`() {
        val assistanceId = UUID.randomUUID()
        every { openAiRequestValidator.validate(request, userProfile) } returns Unit.right()
        every { openAiPreconditionChecker.check(request, userProfile) } returns Unit.right()
        val userDirective = """My code is:
            |User code
            |
            |I am receiving this error message: Error message
            |
            |Help me fix the error in my code in two steps.
            |First, explain the error, why it occurs, and give me a hint for how to fix it.        
            |Second, give me the corrected code.
            |
            |Respond according to the schema:
            |{
            |"ExplanationAndHint": "Error explanation and hint for how to fix it",
            |"CorrectCode": "Only the code that fixes the error"
            |}
            |
            |Ensure that the response strictly follows this structure
            |Respond in a JSON format
            |
            |For all subsequent messages, respond with standard error-fixing guidance or code improvements without adhering to the JSON schema.""".trimMargin()
        every {
            openAiService.startAssistance(
                userDirective,
                chatOptions
            )
        } returns "Your code is incorrect!"
        every { openAiAssistanceService.existsBySessionId(sessionId) } returns false
        every {
            openAiAssistanceService.save(
                userProfile,
                userDirective,
                "Your code is incorrect!",
                request
            )
        } returns OpenAiAssistance(assistanceId, userId, UserType.STUDENT)
        every { tipTokensService.withdrawOneTipToken(userProfile) } returns Unit
        getOpenAiAssistanceOnErrorUseCase.getAssistance(request, userProfile).fold(
            { fail() }, { verify { openAiService.startAssistance(userDirective, chatOptions) } }
        )
    }

    @Test
    fun `should return content`() {
        val assistanceId = UUID.randomUUID()
        every { openAiRequestValidator.validate(request, userProfile) } returns Unit.right()
        every { openAiPreconditionChecker.check(request, userProfile) } returns Unit.right()
        val userDirective = """My code is:
            |User code
            |
            |I am receiving this error message: Error message
            |
            |Help me fix the error in my code in two steps.
            |First, explain the error, why it occurs, and give me a hint for how to fix it.        
            |Second, give me the corrected code.
            |
            |Respond according to the schema:
            |{
            |"ExplanationAndHint": "Error explanation and hint for how to fix it",
            |"CorrectCode": "Only the code that fixes the error"
            |}
            |
            |Ensure that the response strictly follows this structure
            |Respond in a JSON format
            |
            |For all subsequent messages, respond with standard error-fixing guidance or code improvements without adhering to the JSON schema.""".trimMargin()
        every {
            openAiService.startAssistance(
                userDirective,
                chatOptions
            )
        } returns "Your code is incorrect!"
        every { openAiAssistanceService.existsBySessionId(sessionId) } returns false
        every {
            openAiAssistanceService.save(
                userProfile,
                userDirective,
                "Your code is incorrect!",
                request
            )
        } returns OpenAiAssistance(assistanceId, userId, UserType.STUDENT)
        every { tipTokensService.withdrawOneTipToken(userProfile) } returns Unit
        getOpenAiAssistanceOnErrorUseCase.getAssistance(request, userProfile).fold(
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
        every { openAiRequestValidator.validate(request, userProfile) } returns Unit.right()
        every { openAiPreconditionChecker.check(request, userProfile) } returns Unit.right()
        val userDirective = """My code is:
            |User code
            |
            |I am receiving this error message: Error message
            |
            |Help me fix the error in my code in two steps.
            |First, explain the error, why it occurs, and give me a hint for how to fix it.        
            |Second, give me the corrected code.
            |
            |Respond according to the schema:
            |{
            |"ExplanationAndHint": "Error explanation and hint for how to fix it",
            |"CorrectCode": "Only the code that fixes the error"
            |}
            |
            |Ensure that the response strictly follows this structure
            |Respond in a JSON format
            |
            |For all subsequent messages, respond with standard error-fixing guidance or code improvements without adhering to the JSON schema.""".trimMargin()
        every {
            openAiService.startAssistance(
                userDirective,
                chatOptions
            )
        } returns "Your code is incorrect!"
        every { openAiAssistanceService.existsBySessionId(sessionId) } returns false
        every {
            openAiAssistanceService.save(
                userProfile,
                userDirective,
                "Your code is incorrect!",
                request
            )
        } returns OpenAiAssistance(assistanceId, userId, UserType.STUDENT)
        every { tipTokensService.withdrawOneTipToken(userProfile) } returns Unit
        getOpenAiAssistanceOnErrorUseCase.getAssistance(request, userProfile)
            .fold({ fail() }, { verify { tipTokensService.withdrawOneTipToken(userProfile) } })
    }
}
