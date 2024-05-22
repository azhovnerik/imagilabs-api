package com.anahoret.imagilabsapi.openai.domain

import com.anahoret.imagilabsapi.openai.domain.usecases.QuestionAssistanceRequest
import com.anahoret.imagilabsapi.openai.storage.OpenAiAssistanceEntity
import com.anahoret.imagilabsapi.openai.storage.OpenAiAssistanceRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import java.util.*

@DisplayName("Open AI assistance service")
class OpenAiAssistanceServiceImplTest {

    private val openAiAssistanceRepository = mockk<OpenAiAssistanceRepository>()
    private val openAiAssistanceService = OpenAiAssistanceServiceImpl(openAiAssistanceRepository)

    @DisplayName("When save AI assistance")
    @Nested
    inner class Save {

        private val userId = UUID.randomUUID()
        private val projectId = UUID.randomUUID()
        private val userQuestion = "What is the latest version of Python?"
        private val aiResponse = "The latest version of Python is 3.12.2"

        @Test
        fun `should save AI assistance data`() {
            val request = QuestionAssistanceRequest(UUID.randomUUID(), projectId, "User code", "What is 'm' in my code?")
            val assistanceId = UUID.randomUUID()
            val slot = slot<OpenAiAssistanceEntity>()
            every { openAiAssistanceRepository.save(capture(slot)) } answers {
                val entity = slot.captured
                entity.id = assistanceId
                entity
            }
            val result = openAiAssistanceService.save(userId, userQuestion, aiResponse, request)
            assertAll(
                { assertEquals(assistanceId, result.id) },
                { assertEquals(userId, result.userId) }
            )
        }
    }

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

    @DisplayName("When get by id")
    @Nested
    inner class GetById {

        private val assistanceId = UUID.randomUUID()
        private val testUserId = UUID.randomUUID()

        @Test
        fun `should return null when assistance doesn't exist`() {
            every { openAiAssistanceRepository.findByIdOrNull(assistanceId) } returns null
            assertNull(openAiAssistanceService.getById(assistanceId))
        }

        @Test
        fun `should return assistance when it exists`() {
            every { openAiAssistanceRepository.findByIdOrNull(assistanceId) } returns mockk {
                every { id } returns assistanceId
                every { userId } returns testUserId
            }
            val result = openAiAssistanceService.getById(assistanceId)
            assertAll(
                { assertEquals(assistanceId, result!!.id) },
                { assertEquals(testUserId, result!!.userId) }
            )
        }
    }

    @DisplayName("When leave feedback")
    @Nested
    inner class LeaveFeedback {

        private val id = UUID.randomUUID()

        @Test
        fun `shouldn't update when assistance not found`() {
            every { openAiAssistanceRepository.findByIdOrNull(id) } returns null
            openAiAssistanceService.leaveFeedback(id, true)
            verify(exactly = 0) { openAiAssistanceRepository.save(allAny()) }
        }

        @Test
        fun `should update assistance when it exists`() {
            val entity = mockk<OpenAiAssistanceEntity>()
            every { openAiAssistanceRepository.findByIdOrNull(id) } returns entity
            every { entity setProperty "isHelpful" value true } just runs
            every { openAiAssistanceRepository.save(entity) } returns entity
            openAiAssistanceService.leaveFeedback(id, true)
            verify { openAiAssistanceRepository.save(entity) }
        }
    }
}
