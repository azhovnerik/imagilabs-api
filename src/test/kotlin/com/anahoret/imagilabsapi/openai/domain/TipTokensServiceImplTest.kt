package com.anahoret.imagilabsapi.openai.domain

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.students.storage.StudentProfileEntity
import com.anahoret.imagilabsapi.students.storage.StudentProfileEntityRepository
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntity
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntityRepository
import com.anahoret.imagilabsapi.users.UserType
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
    private val teacherProfileEntityRepository = mockk<TeacherProfileEntityRepository>()
    private val tipTokens = 5
    private val tipTokensService =
        TipTokensServiceImpl(studentProfileEntityRepository, teacherProfileEntityRepository, tipTokens)
    private val studentId = UUID.randomUUID()
    private val teacherId = UUID.randomUUID()
    private val studentProfile = mockk<UserProfile>() {
        every { id } returns studentId
        every { userType } returns UserType.STUDENT
    }
    private val teacherProfile = mockk<UserProfile>() {
        every { id } returns teacherId
        every { userType } returns UserType.TEACHER
    }

    @DisplayName("When get student tip tokens")
    @Nested
    inner class GetStudentTipTokens {

        @Test
        fun `should return null when user not found`() {
            every { studentProfileEntityRepository.findByIdOrNull(studentId) } returns null
            assertNull(tipTokensService.getTipTokens(studentProfile))
        }

        @Test
        fun `should return tip tokens when student exists`() {
            every { studentProfileEntityRepository.findByIdOrNull(studentId) } returns mockk {
                every { tipTokens } returns 4
            }
            assertEquals(4, tipTokensService.getTipTokens(studentProfile))
        }
    }

    @DisplayName("When replenish tip tokens")
    @Nested
    inner class ReplenishTipTokens {

        @Test
        fun `should refresh tip tokens`() {
            val student = mockk<StudentProfileEntity>()
            val students = listOf(student)

            val teacher = mockk<TeacherProfileEntity>()
            val teachers = listOf(teacher)

            every { studentProfileEntityRepository.findAll() } returns students
            every { student setProperty "tipTokens" value 5 } just runs
            every { studentProfileEntityRepository.saveAll(students) } returns students
            every { teacherProfileEntityRepository.findAll() } returns teachers
            every { teacher setProperty "tipTokens" value 5 } just runs
            every { teacherProfileEntityRepository.saveAll(teachers) } returns teachers
            tipTokensService.replenishTipTokens()
            verify { student setProperty "tipTokens" value 5 }
        }
    }

    @DisplayName("When withdraw one tip tokens")
    @Nested
    inner class WithdrawOneTipToken {

        @Test
        fun `shouldn't withdraw one tip token when student not found`() {
            every { studentProfileEntityRepository.findByIdOrNull(studentId) } returns null
            tipTokensService.withdrawOneTipToken(studentProfile)
            verify(exactly = 0) { studentProfileEntityRepository.save(allAny()) }
        }

        @Test
        fun `shouldn't withdraw one tip token when teacher not found`() {
            every { teacherProfileEntityRepository.findByIdOrNull(teacherId) } returns null
            tipTokensService.withdrawOneTipToken(teacherProfile)
            verify(exactly = 0) { teacherProfileEntityRepository.save(allAny()) }
        }

        @Test
        fun `should withdraw one tip token when student exists`() {
            val entity = mockk<StudentProfileEntity> {
                every { tipTokens } returns 3
            }
            every { studentProfileEntityRepository.findByIdOrNull(studentId) } returns entity
            every { entity setProperty "tipTokens" value 2 } just runs
            every { studentProfileEntityRepository.save(entity) } returns entity
            tipTokensService.withdrawOneTipToken(studentProfile)
            verify { entity setProperty "tipTokens" value 2 }
        }

        @Test
        fun `should withdraw one tip token when teacher exists`() {
            val entity = mockk<TeacherProfileEntity> {
                every { tipTokens } returns 3
            }
            every { teacherProfileEntityRepository.findByIdOrNull(teacherId) } returns entity
            every { entity setProperty "tipTokens" value 2 } just runs
            every { teacherProfileEntityRepository.save(entity) } returns entity
            tipTokensService.withdrawOneTipToken(teacherProfile)
            verify { entity setProperty "tipTokens" value 2 }
        }
    }

    @DisplayName("When has tip tokens")
    @Nested
    inner class HasTipTokens {

        @Test
        fun `should return null when student not found`() {
            every { studentProfileEntityRepository.findByIdOrNull(studentId) } returns null
            assertNull(tipTokensService.hasTipTokens(studentProfile))
        }

        @Test
        fun `should return null when teacher not found`() {
            every { teacherProfileEntityRepository.findByIdOrNull(teacherId) } returns null
            assertNull(tipTokensService.hasTipTokens(teacherProfile))
        }

        @Test
        fun `should return false when student do not have tip tokens`() {
            every { studentProfileEntityRepository.findByIdOrNull(studentId) } returns mockk {
                every { tipTokens } returns 0
            }
            assertFalse(tipTokensService.hasTipTokens(studentProfile)!!)
        }

        @Test
        fun `should return false when teacher do not have tip tokens`() {
            every { teacherProfileEntityRepository.findByIdOrNull(teacherId) } returns mockk {
                every { tipTokens } returns 0
            }
            assertFalse(tipTokensService.hasTipTokens(teacherProfile)!!)
        }

        @Test
        fun `should return true when student has tip tokens`() {
            every { studentProfileEntityRepository.findByIdOrNull(studentId) } returns mockk {
                every { tipTokens } returns 2
            }
            assertTrue(tipTokensService.hasTipTokens(studentProfile)!!)
        }

        @Test
        fun `should return true when teacher has tip tokens`() {
            every { teacherProfileEntityRepository.findByIdOrNull(teacherId) } returns mockk {
                every { tipTokens } returns 2
            }
            assertTrue(tipTokensService.hasTipTokens(teacherProfile)!!)
        }
    }
}
