package com.anahoret.imagilabsapi.openai.domain.usecases

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.openai.domain.OpenAiAccessService
import com.anahoret.imagilabsapi.openai.domain.OpenAiAssistanceService
import com.anahoret.imagilabsapi.projects.domain.Project
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.*

@DisplayName("Open AI request validator")
class OpenAiRequestValidatorImplTest {

    private val projectService = mockk<ProjectService>()
    private val openAiAccessService = mockk<OpenAiAccessService>()
    private val openAiAssistanceService = mockk<OpenAiAssistanceService>()
    private val openAiRequestValidator =
        OpenAiRequestValidatorImpl(projectService, openAiAccessService, openAiAssistanceService)

    private val userId = UUID.randomUUID()
    private val user = mockk<UserProfile> {
        every { id } returns userId
    }
    private val projectId = UUID.randomUUID()
    private val sessionId = UUID.randomUUID()
    private val request = QuestionAssistanceRequest(sessionId, projectId, "User code", "What is 'm' in my code?")

    @ParameterizedTest
    @ValueSource(strings = ["", "  "])
    fun `should return error when user question is blank`(question: String) {
        val requestWithEmptyQuestion = QuestionAssistanceRequest(sessionId, projectId, "User code", question)
        every { projectService.getProjectById(projectId) } returns null
        when (val res = openAiRequestValidator.validate(user, requestWithEmptyQuestion)) {
            is Either.Left -> assertAll(
                { assertTrue(res.value is ValidationError) },
                { assertEquals("USER_QUESTION_IS_BLANK", (res.value as ValidationError).message) }
            )

            is Either.Right -> fail()
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "  "])
    fun `should return error when user error is blank`(errorMessage: String) {
        val requestWithEmptyQuestion = ErrorAssistanceRequest(sessionId, projectId, "User code", errorMessage)
        every { projectService.getProjectById(projectId) } returns null
        when (val res = openAiRequestValidator.validate(user, requestWithEmptyQuestion)) {
            is Either.Left -> assertAll(
                { assertTrue(res.value is ValidationError) },
                { assertEquals("ERROR_MESSAGE_IS_BLANK", (res.value as ValidationError).message) }
            )

            is Either.Right -> fail()
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "  "])
    fun `should return error when user code is blank`(code: String) {
        val requestWithEmptyQuestion = QuestionAssistanceRequest(sessionId, projectId, code, "What is 'm' in my code?")
        every { projectService.getProjectById(projectId) } returns null
        when (val res = openAiRequestValidator.validate(user, requestWithEmptyQuestion)) {
            is Either.Left -> assertAll(
                { assertTrue(res.value is ValidationError) },
                { assertEquals("USER_CODE_IS_BLANK", (res.value as ValidationError).message) }
            )

            is Either.Right -> fail()
        }
    }

    @Test
    fun `should return error when project doesn't exist`() {
        every { projectService.getProjectById(projectId) } returns null
        when (val res = openAiRequestValidator.validate(user, request)) {
            is Either.Left -> assertAll(
                { assertTrue(res.value is NotFoundError) },
                { assertEquals("PROJECT_NOT_FOUND", (res.value as NotFoundError).message) }
            )

            is Either.Right -> fail()
        }
    }

    @Test
    fun `should return error when has no access to get assistance`() {
        val project = mockk<Project>()
        every { projectService.getProjectById(projectId) } returns project
        every { openAiAccessService.canGetAssistance(user, project) } returns false
        when (val res = openAiRequestValidator.validate(user, request)) {
            is Either.Left -> assertAll(
                { assertTrue(res.value is AccessDeniedError) },
                { assertEquals("ACCESS_TO_OPEN_AI_DENIED", (res.value as AccessDeniedError).message) }
            )

            is Either.Right -> fail()
        }
    }

    @Test
    fun `should return error when session id already exists`() {
        val project = mockk<Project>()
        every { projectService.getProjectById(projectId) } returns project
        every { openAiAccessService.canGetAssistance(user, project) } returns true
        every { openAiAssistanceService.existsBySessionId(sessionId) } returns true
        when (val res = openAiRequestValidator.validate(user, request)) {
            is Either.Left -> assertAll(
                { assertTrue(res.value is ValidationError) },
                { assertEquals("SESSION_ID_ALREADY_EXISTS", (res.value as ValidationError).message) }
            )

            is Either.Right -> fail()
        }
    }

}
