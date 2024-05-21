package com.anahoret.imagilabsapi.debuggingbuddy.service

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.openai.domain.OpenAiAccessServiceImpl
import com.anahoret.imagilabsapi.projects.domain.Project
import com.anahoret.imagilabsapi.projects.domain.ProjectAccessService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Open AI access service")
class OpenAiAccessServiceImplTest {

    private val projectAccessService = mockk<ProjectAccessService>()
    private val aiAccessService = OpenAiAccessServiceImpl(projectAccessService)

    @Nested
    @DisplayName("When can get assistance")
    inner class CanGetAssistance {

        private val userProfile = mockk<UserProfile>()
        private val project = mockk<Project>()

        @Test
        fun `should return true when user is project owner`() {
            every { projectAccessService.isProjectOwner(userProfile, project) } returns true
            assertTrue(aiAccessService.canGetAssistance(userProfile, project))
        }

        @Test
        fun `should return false when user isn't project owner`() {
            every { projectAccessService.isProjectOwner(userProfile, project) } returns false
            assertFalse(aiAccessService.canGetAssistance(userProfile, project))
        }
    }
}
