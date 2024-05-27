package com.anahoret.imagilabsapi.openai.domain

import com.anahoret.imagilabsapi.openai.storage.TipTokensRepository
import com.anahoret.imagilabsapi.students.storage.StudentProfileEntity
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import java.util.*

@DisplayName("Tip tokens service")
class TipTokensServiceImplTest {

    private val tipTokensRepository = mockk<TipTokensRepository>()
    private val tipTokens = 5
    private val tipTokensService = TipTokensServiceImpl(tipTokensRepository, tipTokens)

    @DisplayName("When get student tip tokens")
    @Nested
    inner class GetStudentTipTokens {

        private val studentId = UUID.randomUUID()

        @Test
        fun `should return null when student doesn't exist`() {
            every { tipTokensRepository.findByIdOrNull(studentId) } returns null
            assertNull(tipTokensService.getStudentTipTokens(studentId))
        }

        @Test
        fun `should return tip tokens when student exists`() {
            every { tipTokensRepository.findByIdOrNull(studentId) } returns mockk()
            every { tipTokensRepository.getStudentTipTokens(studentId) } returns 4
            assertEquals(4, tipTokensService.getStudentTipTokens(studentId))
        }
    }

    @DisplayName("When replenish tip tokens")
    @Nested
    inner class ReplenishTipTokens {

        @Test
        fun `should refresh tip tokens`() {
            every { tipTokensRepository.updateTipTokens(tipTokens) } returns Unit
            tipTokensService.replenishTipTokens()
            verify { tipTokensRepository.updateTipTokens(tipTokens) }
        }
    }

    @DisplayName("When withdraw one tip tokens")
    @Nested
    inner class WithdrawOneTipToken {

        private val studentId = UUID.randomUUID()

        @Test
        fun `shouldn't withdraw one tip token when student not found`() {
            every { tipTokensRepository.findByIdOrNull(studentId) } returns null
            tipTokensService.withdrawOneTipToken(studentId)
            verify(exactly = 0) { tipTokensRepository.save(allAny()) }
        }

        @Test
        fun `should withdraw one tip token when student exists`() {
            val entity = mockk<StudentProfileEntity> {
                every { tipTokens } returns 3
            }
            every { tipTokensRepository.findByIdOrNull(studentId) } returns entity
            every { entity setProperty "tipTokens" value 2 } just runs
            every { tipTokensRepository.save(entity) } returns entity
            tipTokensService.withdrawOneTipToken(studentId)
            verify { entity setProperty "tipTokens" value 2 }
        }
    }

    @DisplayName("When has tip tokens")
    @Nested
    inner class HasTipTokens {

        private val studentId = UUID.randomUUID()

        @Test
        fun `should return false when student not found`() {
            every { tipTokensRepository.findByIdOrNull(studentId) } returns null
            assertFalse(tipTokensService.hasTipTokens(studentId))
        }

        @Test
        fun `should return false when student exists and do not have tip tokens`() {
            val entity = mockk<StudentProfileEntity>()
            every { tipTokensRepository.findByIdOrNull(studentId) } returns entity
            every { tipTokensRepository.hasTipTokens(studentId) } returns false
            assertFalse(tipTokensService.hasTipTokens(studentId))
        }

        @Test
        fun `should return true when student exists and have tip tokens`() {
            val entity = mockk<StudentProfileEntity>()
            every { tipTokensRepository.findByIdOrNull(studentId) } returns entity
            every { tipTokensRepository.hasTipTokens(studentId) } returns true
            assertTrue(tipTokensService.hasTipTokens(studentId))
        }
    }
}
