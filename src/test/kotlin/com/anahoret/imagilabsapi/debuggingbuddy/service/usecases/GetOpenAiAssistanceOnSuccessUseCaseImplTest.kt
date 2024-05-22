package com.anahoret.imagilabsapi.debuggingbuddy.service.usecases

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.openai.domain.*
import com.anahoret.imagilabsapi.openai.domain.usecases.GetOpenAiAssistanceOnSuccessUseCaseImpl
import com.anahoret.imagilabsapi.openai.domain.usecases.OpenAiRequestValidator
import com.anahoret.imagilabsapi.openai.domain.usecases.QuestionAssistanceRequest
import com.anahoret.imagilabsapi.projects.domain.Project
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("Get Open Ai assistance on success use case")
class GetOpenAiAssistanceOnSuccessUseCaseImplTest {

    private val projectService = mockk<ProjectService>()
    private val openAiAccessService = mockk<OpenAiAccessService>()
    private val openAiService = mockk<OpenAiService>()
    private val openAiAssistanceService = mockk<OpenAiAssistanceService>()
    private val openAiRequestValidator = mockk<OpenAiRequestValidator>()
    private val getOpenAiAssistanceOnSuccessUseCase =
        GetOpenAiAssistanceOnSuccessUseCaseImpl(
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
    fun `should generate secondDirectiveWithQuestion`() {
        val assistanceId = UUID.randomUUID()
        val secondDirectiveWithQuestion = "My question is: What is 'm' in my code? ${OpenAiPrompts.SECOND_DIRECTIVE}"
        val project = mockk<Project> {
            every { id } returns projectId
        }
        every { projectService.getProjectById(projectId) } returns project
        every { openAiAccessService.canGetAssistance(user, project) } returns true
        every { openAiService.startAssistance("User code", secondDirectiveWithQuestion) } returns mockk {
            every { results } returns listOf(mockk {
                every { output } returns mockk {
                    every { content } returns "Great result!"
                }
            })
        }
        every { openAiAssistanceService.existsBySessionId(sessionId) } returns false
        every {
            openAiAssistanceService.save(
                sessionId,
                userId,
                projectId,
                "What is 'm' in my code?",
                "Great result!"
            )
        } returns OpenAiAssistance(assistanceId, userId)
        when (getOpenAiAssistanceOnSuccessUseCase.get(user, request)) {
            is Either.Left -> fail()
            is Either.Right -> verify { openAiService.startAssistance("User code", secondDirectiveWithQuestion) }
        }
    }

    @Test
    fun `should return content`() {
        val assistanceId = UUID.randomUUID()
        val secondDirectiveWithQuestion = "My question is: What is 'm' in my code? ${OpenAiPrompts.SECOND_DIRECTIVE}"
        val project = mockk<Project> {
            every { id } returns projectId
        }
        every { projectService.getProjectById(projectId) } returns project
        every { openAiAccessService.canGetAssistance(user, project) } returns true
        every { openAiService.startAssistance("User code", secondDirectiveWithQuestion) } returns mockk {
            every { results } returns listOf(mockk {
                every { output } returns mockk {
                    every { content } returns "Great result!"
                }
            })
        }
        every { openAiAssistanceService.existsBySessionId(sessionId) } returns false
        every {
            openAiAssistanceService.save(
                sessionId,
                userId,
                projectId,
                "What is 'm' in my code?",
                "Great result!"
            )
        } returns OpenAiAssistance(assistanceId, userId)
        when (val res = getOpenAiAssistanceOnSuccessUseCase.get(user, request)) {
            is Either.Left -> fail()
            is Either.Right -> assertAll(
                { assertEquals("Great result!", res.value.aiResponse) },
                { assertEquals(assistanceId, res.value.assistanceId) }
            )
        }
    }
}
