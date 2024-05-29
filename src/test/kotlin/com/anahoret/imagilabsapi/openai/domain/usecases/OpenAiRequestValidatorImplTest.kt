package com.anahoret.imagilabsapi.openai.domain.usecases

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import io.mockk.every
import io.mockk.mockk
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.*

@DisplayName("Open AI request validator")
class OpenAiRequestValidatorImplTest {

    private val openAiRequestValidator = OpenAiRequestValidatorImpl()

    private val userId = UUID.randomUUID()
    private val user = mockk<UserProfile> {
        every { id } returns userId
    }
    private val projectId = UUID.randomUUID()
    private val sessionId = UUID.randomUUID()

    @ParameterizedTest
    @ValueSource(strings = ["", "  "])
    fun `should return error when user question is blank`(question: String) {
        val request = QuestionAssistanceRequest(sessionId, projectId, "User code", question)
        openAiRequestValidator.validate(request, user).fold(
            {
                assertAll(
                    { assertTrue(it is ValidationError) },
                    { assertEquals("USERQUESTION_IS_BLANK", (it as ValidationError).message) }
                )
            },
            { fail() }
        )
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "  "])
    fun `should return error when user error is blank`(errorMessage: String) {
        val request = ErrorAssistanceRequest(sessionId, projectId, "User code", errorMessage)
        openAiRequestValidator.validate(request, user).fold(
            {
                assertAll(
                    { assertTrue(it is ValidationError) },
                    { assertEquals("ERRORMESSAGE_IS_BLANK", (it as ValidationError).message) }
                )
            },
            { fail() }
        )
    }


    @ParameterizedTest
    @ValueSource(strings = ["", "  "])
    fun `should return error when user input is blank`(input: String) {
        val request = ProceedAssistanceRequest(sessionId, projectId, input)
        openAiRequestValidator.validate(request, user).fold(
            {
                assertAll(
                    { assertTrue(it is ValidationError) },
                    { assertEquals("INPUT_IS_BLANK", (it as ValidationError).message) }
                )
            },
            { fail() }
        )
    }


    @ParameterizedTest
    @ValueSource(strings = ["", "  "])
    fun `should return error when user code is blank and request is QuestionAssistanceRequest`(code: String) {
        val request = QuestionAssistanceRequest(sessionId, projectId, code, "What is 'm' in my code?")
        openAiRequestValidator.validate(request, user).fold(
            {
                assertAll(
                    { assertTrue(it is ValidationError) },
                    { assertEquals("USERCODE_IS_BLANK", (it as ValidationError).message) }
                )
            },
            { fail() }
        )
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "  "])
    fun `should return error when user code is blank and request is ErrorAssistanceRequest`(code: String) {
        val request = ErrorAssistanceRequest(sessionId, projectId, code, "Error message")
        openAiRequestValidator.validate(request, user).fold(
            {
                assertAll(
                    { assertTrue(it is ValidationError) },
                    { assertEquals("USERCODE_IS_BLANK", (it as ValidationError).message) }
                )
            },
            { fail() }
        )
    }

    @Test
    fun `should return error when user question length is greater than 300 characters and request is QuestionAssistanceRequest`() {
        val questionLongerThan300Chars = RandomStringUtils.random(301)
        val request = QuestionAssistanceRequest(sessionId, projectId, "code", questionLongerThan300Chars)
        openAiRequestValidator.validate(request, user).fold(
            {
                assertAll(
                    { assertTrue(it is ValidationError.FieldIsTooLong) },
                    { assertEquals("USERQUESTION_IS_TOO_LONG", (it as ValidationError).message) }
                )
            },
            { fail() }
        )
    }

    @Test
    fun `should return Unit when user question length is 300 characters and request is QuestionAssistanceRequest`() {
        val questionWithLength300Chars = RandomStringUtils.random(300)
        val request = QuestionAssistanceRequest(sessionId, projectId, "code", questionWithLength300Chars)
        openAiRequestValidator.validate(request, user).fold({ fail() }, { assertEquals(Unit, it) })
    }

    @Test
    fun `should return error when user input length is longer than 300 characters and request is ProceedAssistanceRequest`() {
        val inputLongerThan300Chars = RandomStringUtils.random(301)
        val request = ProceedAssistanceRequest(sessionId, projectId, inputLongerThan300Chars)
        openAiRequestValidator.validate(request, user).fold(
            {
                assertAll(
                    { assertTrue(it is ValidationError.FieldIsTooLong) },
                    { assertEquals("INPUT_IS_TOO_LONG", (it as ValidationError).message) }
                )
            },
            { fail() }
        )
    }

    @Test
    fun `should return Unit when user input length is  300 characters and request is ProceedAssistanceRequest`() {
        val inputWithLength300Chars = RandomStringUtils.random(300)
        val request = ProceedAssistanceRequest(sessionId, projectId, inputWithLength300Chars)
        openAiRequestValidator.validate(request, user).fold({ fail() }, { assertEquals(Unit, it) })
    }

    @Test
    fun `should return Unit when user input is valid`() {
        val request = ProceedAssistanceRequest(sessionId, projectId, "input")
        openAiRequestValidator.validate(request, user).fold({ fail() }, { })
    }
}
