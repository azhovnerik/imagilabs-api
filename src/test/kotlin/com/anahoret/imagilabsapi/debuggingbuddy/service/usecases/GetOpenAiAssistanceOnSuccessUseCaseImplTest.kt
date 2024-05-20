package com.anahoret.imagilabsapi.debuggingbuddy.service.usecases

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.openai.domain.OpenAiAccessService
import com.anahoret.imagilabsapi.openai.domain.OpenAiAssistanceService
import com.anahoret.imagilabsapi.openai.domain.OpenAiPrompts
import com.anahoret.imagilabsapi.openai.domain.OpenAiService
import com.anahoret.imagilabsapi.openai.domain.usecases.GetOpenAiAssistanceOnSuccessUseCaseImpl
import com.anahoret.imagilabsapi.openai.web.OpenAiController.AssistanceOnSuccessRequest
import com.anahoret.imagilabsapi.projects.domain.Project
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.*

@DisplayName("Get Open Ai assistance on success use case")
class GetOpenAiAssistanceOnSuccessUseCaseImplTest {

    private val projectService = mockk<ProjectService>()
    private val openAiAccessService = mockk<OpenAiAccessService>()
    private val openAiService = mockk<OpenAiService>()
    private val openAiAssistanceService = mockk<OpenAiAssistanceService>()
    private val getOpenAiAssistanceOnSuccessUseCase =
        GetOpenAiAssistanceOnSuccessUseCaseImpl(
            projectService,
            openAiAccessService,
            openAiService,
            openAiAssistanceService
        )

    private val userId = UUID.randomUUID()
    private val user = mockk<UserProfile> {
        every { id } returns userId
    }
    private val projectId = UUID.randomUUID()
    private val request = AssistanceOnSuccessRequest(projectId, "User code", "What is 'm' in my code?")

    @ParameterizedTest
    @ValueSource(strings = ["", "  "])
    fun `should return error when user question is blank`(question: String) {
        val requestWithEmptyQuestion = AssistanceOnSuccessRequest(projectId, "User code", question)
        every { projectService.getProjectById(projectId) } returns null
        when (val res = getOpenAiAssistanceOnSuccessUseCase.get(user, requestWithEmptyQuestion)) {
            is Either.Left -> assertAll(
                { assertTrue(res.value is ValidationError) },
                { assertEquals("USER_QUESTION_IS_BLANK", (res.value as ValidationError).message) }
            )

            is Either.Right -> fail()
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "  "])
    fun `should return error when user code is blank`(code: String) {
        val requestWithEmptyQuestion = AssistanceOnSuccessRequest(projectId, code, "What is 'm' in my code?")
        every { projectService.getProjectById(projectId) } returns null
        when (val res = getOpenAiAssistanceOnSuccessUseCase.get(user, requestWithEmptyQuestion)) {
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
        when (val res = getOpenAiAssistanceOnSuccessUseCase.get(user, request)) {
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
        when (val res = getOpenAiAssistanceOnSuccessUseCase.get(user, request)) {
            is Either.Left -> assertAll(
                { assertTrue(res.value is AccessDeniedError) },
                { assertEquals("ACCESS_TO_OPEN_AI_DENIED", (res.value as AccessDeniedError).message) }
            )

            is Either.Right -> fail()
        }
    }

    @Test
    fun `should generate secondDirectiveWithQuestion`() {
        val secondDirectiveWithQuestion = "My question is: What is 'm' in my code? ${OpenAiPrompts.SECOND_DIRECTIVE}"
        val project = mockk<Project> {
            every { id } returns projectId
        }
        every { projectService.getProjectById(projectId) } returns project
        every { openAiAccessService.canGetAssistance(user, project) } returns true
        every { openAiService.getAssistanceOnSuccess(request, secondDirectiveWithQuestion) } returns mockk {
            every { results } returns listOf(mockk {
                every { output } returns mockk {
                    every { content } returns "Great result!"
                }
            })
        }
        every {
            openAiAssistanceService.save(
                userId,
                projectId,
                "What is 'm' in my code?",
                "Great result!"
            )
        } returns Unit
        when (getOpenAiAssistanceOnSuccessUseCase.get(user, request)) {
            is Either.Left -> fail()
            is Either.Right -> verify { openAiService.getAssistanceOnSuccess(request, secondDirectiveWithQuestion) }
        }
    }

    @Test
    fun `should return content`() {
        val secondDirectiveWithQuestion = "My question is: What is 'm' in my code? ${OpenAiPrompts.SECOND_DIRECTIVE}"
        val project = mockk<Project> {
            every { id } returns projectId
        }
        every { projectService.getProjectById(projectId) } returns project
        every { openAiAccessService.canGetAssistance(user, project) } returns true
        every { openAiService.getAssistanceOnSuccess(request, secondDirectiveWithQuestion) } returns mockk {
            every { results } returns listOf(mockk {
                every { output } returns mockk {
                    every { content } returns "Great result!"
                }
            })
        }
        every {
            openAiAssistanceService.save(
                userId,
                projectId,
                "What is 'm' in my code?",
                "Great result!"
            )
        } returns Unit
        when (val res = getOpenAiAssistanceOnSuccessUseCase.get(user, request)) {
            is Either.Left -> fail()
            is Either.Right -> assertEquals("Great result!", res.value.aiResponse)
        }
    }
}
