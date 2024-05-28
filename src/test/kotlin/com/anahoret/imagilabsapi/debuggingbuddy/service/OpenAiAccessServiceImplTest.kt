package com.anahoret.imagilabsapi.debuggingbuddy.service

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.openai.domain.OpenAiAccessServiceImpl
import com.anahoret.imagilabsapi.openai.domain.TipTokensService
import com.anahoret.imagilabsapi.projects.domain.Project
import com.anahoret.imagilabsapi.projects.domain.ProjectAccessService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

@DisplayName("Open AI access service")
class OpenAiAccessServiceImplTest {

    private val projectAccessService = mockk<ProjectAccessService>()
    private val tipTokensService = mockk<TipTokensService>()
    private val aiAccessService = OpenAiAccessServiceImpl(projectAccessService, tipTokensService)

    @Nested
    @DisplayName("When can get assistance for project")
    inner class CanGetAssistanceForProject {

        private val userProfile = mockk<UserProfile>()
        private val project = mockk<Project>()

        @Test
        fun `should return true when user is project owner`() {
            every { projectAccessService.isProjectOwner(userProfile, project) } returns true
            assertTrue(aiAccessService.canGetAssistanceForProject(userProfile, project))
        }

        @Test
        fun `should return false when user isn't project owner`() {
            every { projectAccessService.isProjectOwner(userProfile, project) } returns false
            assertFalse(aiAccessService.canGetAssistanceForProject(userProfile, project))
        }
    }

    @Nested
    @DisplayName("When has tip tokens")
    inner class HasTipTokens {

        private val userId = UUID.randomUUID()

        @Test
        fun `should return false when student not found`() {
            every { tipTokensService.hasTipTokens(userId) } returns null
            assertFalse(aiAccessService.hasTipTokens(userId))
        }

        @Test
        fun `should return false when student has not tip tokens`() {
            every { tipTokensService.hasTipTokens(userId) } returns false
            assertFalse(aiAccessService.hasTipTokens(userId))
        }

        @Test
        fun `should return true when student has tip tokens`() {
            every { tipTokensService.hasTipTokens(userId) } returns true
            assertTrue(aiAccessService.hasTipTokens(userId))
        }
    }
}
