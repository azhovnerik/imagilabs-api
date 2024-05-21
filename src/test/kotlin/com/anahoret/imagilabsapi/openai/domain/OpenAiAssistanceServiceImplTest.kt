package com.anahoret.imagilabsapi.openai.domain

import com.anahoret.imagilabsapi.openai.storage.OpenAiAssistanceEntity
import com.anahoret.imagilabsapi.openai.storage.OpenAiAssistanceRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*

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

    @DisplayName("When save AI assistance")
    @Nested
    inner class Save {

        private val userId = UUID.randomUUID()
        private val projectId = UUID.randomUUID()
        private val userQuestion = "What is the latest version of Python?"
        private val aiResponse = "The latest version of Python is 3.12.2"

        @Test
        fun `should save AI assistance data`() {
            val assistanceId = UUID.randomUUID()
            val sessionId = UUID.randomUUID()
            val slot = slot<OpenAiAssistanceEntity>()
            every { openAiAssistanceRepository.save(capture(slot)) } answers {
                val entity = slot.captured
                entity.id = assistanceId
                entity
            }
            val result = openAiAssistanceService.save(sessionId, userId, projectId, userQuestion, aiResponse)
            assertAll(
                { assertEquals(assistanceId, result.assistanceId) },
                { assertEquals(userId, result.userId) }
            )
        }
    }
}
