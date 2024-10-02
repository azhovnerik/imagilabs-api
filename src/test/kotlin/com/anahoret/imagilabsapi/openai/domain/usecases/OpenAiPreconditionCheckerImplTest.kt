package com.anahoret.imagilabsapi.openai.domain.usecases

import arrow.core.handleError
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.openai.domain.OpenAiAccessService
import com.anahoret.imagilabsapi.openai.domain.OpenAiAssistanceService
import com.anahoret.imagilabsapi.projects.domain.Project
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import com.anahoret.imagilabsapi.users.UserType
import com.anahoret.imagilabsapi.utils.OpenAiRequestGenerator.createErrorAssistanceRequest
import com.anahoret.imagilabsapi.utils.OpenAiRequestGenerator.createProceedAssistanceRequest
import com.anahoret.imagilabsapi.utils.OpenAiRequestGenerator.createQuestionAssistanceRequest
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("Open AI precondition checker")
class OpenAiPreconditionCheckerImplTest {

    private val projectService = mockk<ProjectService>()
    private val openAiAccessService = mockk<OpenAiAccessService>()
    private val openAiPreconditionChecker = OpenAiPreconditionCheckerImpl(openAiAccessService)
    private val testProjectId = UUID.randomUUID()
    private val testSessionId = UUID.randomUUID()
    private val request = mockk<AssistanceRequest> {
        every { sessionId } returns testSessionId
    }
    private val userId = UUID.randomUUID()
    private val userProfile = mockk<UserProfile> {
        every { id } returns userId
        every { userType } returns UserType.STUDENT
    }

    @Test
    fun `should return error when user has no access to project`() {
        val project = mockk<Project>()
        every { projectService.getProjectById(testProjectId) } returns project
        every { openAiAccessService.canGetAssistanceForProject(userProfile) } returns false
        openAiPreconditionChecker.check(request, userProfile).fold(
            {
                assertAll(
                    { assertTrue(it is AccessDeniedError) },
                    { assertEquals("ACCESS_TO_PROJECT_DENIED", (it as AccessDeniedError).message) }
                )
            },
            { fail() }
        )
    }

    @Test
    fun `should return error when user has no access to get assistance`() {
        val project = mockk<Project>()
        every { projectService.getProjectById(testProjectId) } returns project
        every { openAiAccessService.canGetAssistanceForProject(userProfile) } returns true
        every { openAiAccessService.hasTipTokens(userProfile) } returns false
        openAiPreconditionChecker.check(request, userProfile).fold(
            {
                assertAll(
                    { assertTrue(it is AccessDeniedError) },
                    { assertEquals("NO_TIP_TOKENS_LEFT", (it as AccessDeniedError).message) }
                )
            },
            { fail() }
        )
    }

    @Test
    fun `should return success when session id already exists for QuestionAssistanceRequest`() {
        val project = mockk<Project>()
        val request = createQuestionAssistanceRequest(sessionId = testSessionId)
        every { projectService.getProjectById(testProjectId) } returns project
        every { openAiAccessService.canGetAssistanceForProject(userProfile) } returns true
        every { openAiAccessService.hasTipTokens(userProfile) } returns true
        openAiPreconditionChecker.check(request, userProfile).handleError { fail() }
    }

    @Test
    fun `should return success when session id already exists for ErrorAssistanceRequest`() {
        val project = mockk<Project>()
        val request = createErrorAssistanceRequest(sessionId = testSessionId)
        every { projectService.getProjectById(testProjectId) } returns project
        every { openAiAccessService.canGetAssistanceForProject(userProfile) } returns true
        every { openAiAccessService.hasTipTokens(userProfile) } returns true
        openAiPreconditionChecker.check(request, userProfile).handleError { fail() }
    }

    @Test
    fun `should not return error when session id already exists for ErrorAssistanceRequest`() {
        val project = mockk<Project>()
        val request = createProceedAssistanceRequest(sessionId = testSessionId)
        every { projectService.getProjectById(testProjectId) } returns project
        every { openAiAccessService.canGetAssistanceForProject(userProfile) } returns true
        every { openAiAccessService.hasTipTokens(userProfile) } returns true
        openAiPreconditionChecker.check(request, userProfile).fold({ fail() }, { })
    }

    @Test
    fun `shouldn't return error when session id not exists for QuestionAssistanceRequest`() {
        val project = mockk<Project>()
        val request = createQuestionAssistanceRequest(sessionId = testSessionId)
        every { projectService.getProjectById(testProjectId) } returns project
        every { openAiAccessService.canGetAssistanceForProject(userProfile) } returns true
        every { openAiAccessService.hasTipTokens(userProfile) } returns true
        openAiPreconditionChecker.check(request, userProfile).fold({ fail() }, { })
    }

    @Test
    fun `shouldn't return error when session id not exists for ErrorAssistanceRequest`() {
        val project = mockk<Project>()
        val request = createErrorAssistanceRequest(sessionId = testSessionId)
        every { projectService.getProjectById(testProjectId) } returns project
        every { openAiAccessService.canGetAssistanceForProject(userProfile) } returns true
        every { openAiAccessService.hasTipTokens(userProfile) } returns true
        openAiPreconditionChecker.check(request, userProfile).fold({ fail() }, { })
    }
}
