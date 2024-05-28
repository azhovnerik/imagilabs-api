package com.anahoret.imagilabsapi.openai.domain

import com.anahoret.imagilabsapi.students.storage.StudentProfileEntity
import com.anahoret.imagilabsapi.students.storage.StudentProfileEntityRepository
import com.google.common.base.Verify.verify
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import java.util.*

@DisplayName("Tip tokens service")
class TipTokensServiceImplTest {

    private val studentProfileEntityRepository = mockk<StudentProfileEntityRepository>()
    private val tipTokens = 5
    private val tipTokensService = TipTokensServiceImpl(studentProfileEntityRepository, tipTokens)

    @DisplayName("When get student tip tokens")
    @Nested
    inner class GetStudentTipTokens {

        private val studentId = UUID.randomUUID()

        @Test
        fun `should return null when user not found`() {
            every { studentProfileEntityRepository.findByIdOrNull(studentId) } returns null
            assertNull(tipTokensService.getStudentTipTokens(studentId))
        }

        @Test
        fun `should return tip tokens when student exists`() {
            every { studentProfileEntityRepository.findByIdOrNull(studentId) } returns mockk {
                every { tipTokens } returns 4
            }
            assertEquals(4, tipTokensService.getStudentTipTokens(studentId))
        }
    }

    @DisplayName("When replenish tip tokens")
    @Nested
    inner class ReplenishTipTokens {

        @Test
        fun `should refresh tip tokens`() {
            val entity = mockk<StudentProfileEntity>()
            val entities = listOf(entity)
            every { studentProfileEntityRepository.findAll() } returns entities
            every { entity setProperty "tipTokens" value 5 } just runs
            every { studentProfileEntityRepository.saveAll(entities) } returns entities
            tipTokensService.replenishTipTokens()
            verify { entity setProperty "tipTokens" value 5 }
        }
    }

    @DisplayName("When withdraw one tip tokens")
    @Nested
    inner class WithdrawOneTipToken {

        private val studentId = UUID.randomUUID()

        @Test
        fun `shouldn't withdraw one tip token when student not found`() {
            every { studentProfileEntityRepository.findByIdOrNull(studentId) } returns null
            tipTokensService.withdrawOneTipToken(studentId)
            verify(exactly = 0) { studentProfileEntityRepository.save(allAny()) }
        }

        @Test
        fun `should withdraw one tip token when student exists`() {
            val entity = mockk<StudentProfileEntity> {
                every { tipTokens } returns 3
            }
            every { studentProfileEntityRepository.findByIdOrNull(studentId) } returns entity
            every { entity setProperty "tipTokens" value 2 } just runs
            every { studentProfileEntityRepository.save(entity) } returns entity
            tipTokensService.withdrawOneTipToken(studentId)
            verify { entity setProperty "tipTokens" value 2 }
        }
    }

    @DisplayName("When has tip tokens")
    @Nested
    inner class HasTipTokens {

        private val studentId = UUID.randomUUID()

        @Test
        fun `should return null when student not found`() {
            every { studentProfileEntityRepository.findByIdOrNull(studentId) } returns null
            assertNull(tipTokensService.hasTipTokens(studentId))
        }

        @Test
        fun `should return false when student do not have tip tokens`() {
            every { studentProfileEntityRepository.findByIdOrNull(studentId) } returns mockk {
                every { tipTokens } returns 0
            }
            assertFalse(tipTokensService.hasTipTokens(studentId)!!)
        }

        @Test
        fun `should return true when student has tip tokens`() {
            every { studentProfileEntityRepository.findByIdOrNull(studentId) } returns mockk {
                every { tipTokens } returns 2
            }
            assertTrue(tipTokensService.hasTipTokens(studentId)!!)
        }
    }
}
