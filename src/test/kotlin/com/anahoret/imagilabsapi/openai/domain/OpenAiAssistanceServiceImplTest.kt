package com.anahoret.imagilabsapi.openai.domain

import com.anahoret.imagilabsapi.openai.storage.OpenAiAssistanceRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

@DisplayName("Open AI assistance service")
class OpenAiAssistanceServiceImplTest {

    private val openAiAssistanceRepository = mockk<OpenAiAssistanceRepository>()
    private val openAiAssistanceService = OpenAiAssistanceServiceImpl(openAiAssistanceRepository)

    @DisplayName("When exists by session id")
    @Nested
    inner class ExistsBySessionId {

        private val sessionId = UUID.randomUUID()

        @Test
        fun `should return true when assistance exists`() {
            every { openAiAssistanceRepository.existsBySessionId(sessionId) } returns true
            assertTrue(openAiAssistanceService.existsBySessionId(sessionId))
        }

        @Test
        fun `should return false when assistance doesn't exist`() {
            every { openAiAssistanceRepository.existsBySessionId(sessionId) } returns false
            assertFalse(openAiAssistanceService.existsBySessionId(sessionId))
        }
    }
}
