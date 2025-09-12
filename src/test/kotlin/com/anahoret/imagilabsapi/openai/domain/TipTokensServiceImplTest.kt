package com.anahoret.imagilabsapi.openai.domain

import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
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
import java.time.Clock
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

@DisplayName("Tip tokens service")
class TipTokensServiceImplTest {

    private val studentProfileEntityRepository = mockk<StudentProfileEntityRepository>()
    private val teacherProfileEntityRepository = mockk<TeacherProfileEntityRepository>()
    private val classroomService = mockk<ClassroomService>()
    private val clock = mockk<Clock>()
    private val tipTokens = 5
    private val tipTokensService =
        TipTokensServiceImpl(
            studentProfileEntityRepository,
            teacherProfileEntityRepository,
            classroomService,
            tipTokens,
            clock
        )
    private val testClassroomId = UUID.randomUUID()
    private val testTeacherId = UUID.randomUUID()
    private val testStudentId = UUID.randomUUID()

    private val studentProfile = mockk<UserProfile> {
        every { id } returns testStudentId
        every { userType } returns UserType.STUDENT
    }
    private val teacherProfile = mockk<UserProfile> {
        every { id } returns testTeacherId
        every { userType } returns UserType.TEACHER
    }

    @DisplayName("When get student tip tokens")
    @Nested
    inner class GetStudentTipTokens {

        @Test
        fun `should return null when user not found`() {
            every { studentProfileEntityRepository.findByIdOrNull(testStudentId) } returns null
            assertNull(tipTokensService.getTipTokens(studentProfile))
        }

        @Test
        fun `should return tip tokens when student exists`() {
            every { studentProfileEntityRepository.findByIdOrNull(testStudentId) } returns mockk {
                every { tipTokens } returns 4
            }
            assertEquals(4, tipTokensService.getTipTokens(studentProfile))
        }
    }

    @DisplayName("When replenish tip tokens")
    @Nested
    inner class ReplenishTipTokens {

        @Test
        fun `should refresh tip tokens if teacher has pro subscription`() {
            val teacher = mockk<TeacherProfileEntity> {
                every { hasProSubscription(100) } returns true
                justRun { tipTokens = 5 }
                justRun { tipTokensReplenishedAt = 100 }
            }
            val teachers = listOf(teacher)
            every { studentProfileEntityRepository.findAll() } returns emptyList()
            every { teacherProfileEntityRepository.findAll() } returns teachers
            every { teacherProfileEntityRepository.saveAll(teachers) } returns teachers
            every { clock.millis() } returns 100

            tipTokensService.replenishTipTokens()
            verify { teacher.tipTokens = 5 }
            verify { teacher.tipTokensReplenishedAt = 100 }
        }

        @Test
        fun `should refresh tip tokens if teacher has standard subscription and free plan replenish period passed`() {
            val lastReplenishDate = ZonedDateTime.now()
            val lastReplenishDateMillis = lastReplenishDate.toInstant().toEpochMilli()
            val now = lastReplenishDate
                .plusMonths(1)
                .plus(Duration.ofMillis(1))
                .toInstant().toEpochMilli()
            val teacher = mockk<TeacherProfileEntity> {
                every { hasProSubscription(now) } returns false
                justRun { tipTokens = 5 }
                justRun { tipTokensReplenishedAt = now }
                every { tipTokensReplenishedAt } returns lastReplenishDateMillis
            }
            val teachers = listOf(teacher)
            every { studentProfileEntityRepository.findAll() } returns emptyList()
            every { teacherProfileEntityRepository.findAll() } returns teachers
            every { teacherProfileEntityRepository.saveAll(teachers) } returns teachers
            every { clock.millis() } returns now

            tipTokensService.replenishTipTokens()
            verify { teacher.tipTokens = 5 }
            verify { teacher.tipTokensReplenishedAt = now }
        }

        @Test
        fun `should not refresh tip tokens if teacher has standard subscription and free plan replenish period has not passed`() {
            val lastReplenishDate = ZonedDateTime.of(2024, 11, 1, 0, 0, 0, 0, ZoneId.of("UTC-7"))
            val lastReplenishDateMillis = lastReplenishDate.toInstant().toEpochMilli()
            val now = lastReplenishDate
                .plusMonths(1)
                .toInstant().toEpochMilli()
            val teacher = mockk<TeacherProfileEntity> {
                every { hasProSubscription(now) } returns false
                justRun { tipTokens = 5 }
                justRun { tipTokensReplenishedAt = now }
                every { tipTokensReplenishedAt } returns lastReplenishDateMillis
            }
            val teachers = listOf(teacher)
            every { studentProfileEntityRepository.findAll() } returns emptyList()
            every { teacherProfileEntityRepository.findAll() } returns teachers
            every { teacherProfileEntityRepository.saveAll(teachers) } returns teachers
            every { clock.millis() } returns now

            tipTokensService.replenishTipTokens()
            verify(inverse = true) { teacher.tipTokens = 5 }
            verify(inverse = true) { teacher.tipTokensReplenishedAt = now }
        }

        @Test
        fun `should refresh tip tokens if teacher has standard subscription and free plan replenish period has not passed and current date between September 16 and October 18 2024`() {
            val lastReplenishDate = ZonedDateTime.of(2024, 9, 16, 0, 0, 0, 0, ZoneId.of("UTC-7"))
            val lastReplenishDateMillis = lastReplenishDate.toInstant().toEpochMilli()
            val now = lastReplenishDate
                .plusSeconds(1)
                .toInstant().toEpochMilli()
            val teacher = mockk<TeacherProfileEntity> {
                every { hasProSubscription(now) } returns false
                justRun { tipTokens = 5 }
                justRun { tipTokensReplenishedAt = now }
                every { tipTokensReplenishedAt } returns lastReplenishDateMillis
            }
            val teachers = listOf(teacher)
            every { studentProfileEntityRepository.findAll() } returns emptyList()
            every { teacherProfileEntityRepository.findAll() } returns teachers
            every { teacherProfileEntityRepository.saveAll(teachers) } returns teachers
            every { clock.millis() } returns now

            tipTokensService.replenishTipTokens()
            verify { teacher.tipTokens = 5 }
            verify { teacher.tipTokensReplenishedAt = now }
        }

        @Test
        fun `should refresh tip tokens if student's teacher has pro subscription`() {
            val student = mockk<StudentProfileEntity> {
                every { id } returns testStudentId
                justRun { tipTokens = 5 }
                justRun { tipTokensReplenishedAt = 100 }
                every { classroomId } returns testClassroomId
            }
            val students = listOf(student)
            val teacher = mockk<TeacherProfileEntity> {
                every { id } returns testTeacherId
                every { hasProSubscription(100) } returns true
                justRun { tipTokens = 5 }
                justRun { tipTokensReplenishedAt = 100 }
            }
            val teachers = listOf(teacher)
            every { teacherProfileEntityRepository.findAll() } returns teachers
            every { teacherProfileEntityRepository.saveAll(teachers) } returns teachers
            every { studentProfileEntityRepository.findAll() } returns students
            every { studentProfileEntityRepository.saveAll(students) } returns students
            every { classroomService.listByIds(listOf(testClassroomId)) } returns listOf(mockk {
                every { id } returns testClassroomId
                every { teacherId } returns testTeacherId
            })
            every { clock.millis() } returns 100
            tipTokensService.replenishTipTokens()
            verify { student.tipTokens = 5 }
            verify { student.tipTokensReplenishedAt = 100 }
        }

        @Test
        fun `should refresh tip tokens if student's teacher has standard subscription and free plan replenish period passed`() {
            val lastReplenishDate = ZonedDateTime.now()
            val lastReplenishDateMillis = lastReplenishDate.toInstant().toEpochMilli()
            val now = lastReplenishDate
                .plusMonths(1)
                .plus(Duration.ofMillis(1))
                .toInstant().toEpochMilli()

            val student = mockk<StudentProfileEntity> {
                every { id } returns testStudentId
                justRun { tipTokens = 5 }
                justRun { tipTokensReplenishedAt = now }
                every { tipTokensReplenishedAt } returns lastReplenishDateMillis
                every { classroomId } returns testClassroomId
            }
            val students = listOf(student)
            val teacher = mockk<TeacherProfileEntity> {
                every { id } returns testTeacherId
                every { hasProSubscription(now) } returns false
                justRun { tipTokens = 5 }
                justRun { tipTokensReplenishedAt = now }
                every { tipTokensReplenishedAt } returns lastReplenishDateMillis
            }
            val teachers = listOf(teacher)
            every { teacherProfileEntityRepository.findAll() } returns teachers
            every { teacherProfileEntityRepository.saveAll(teachers) } returns teachers
            every { studentProfileEntityRepository.findAll() } returns students
            every { studentProfileEntityRepository.saveAll(students) } returns students
            every { classroomService.listByIds(listOf(testClassroomId)) } returns listOf(mockk {
                every { id } returns testClassroomId
                every { teacherId } returns testTeacherId
            })
            every { clock.millis() } returns now
            tipTokensService.replenishTipTokens()
            verify { student.tipTokens = 5 }
            verify { student.tipTokensReplenishedAt = now }
        }

        @Test
        fun `should not refresh tip tokens if student's teacher has standard subscription and free plan replenish period has not passed`() {
            val lastReplenishDate = ZonedDateTime.of(2024, 11, 1, 0, 0, 0, 0, ZoneId.of("UTC-7"))
            val lastReplenishDateMillis = lastReplenishDate.toInstant().toEpochMilli()
            val now = lastReplenishDate
                .plusMonths(1)
                .toInstant().toEpochMilli()

            val student = mockk<StudentProfileEntity> {
                every { id } returns testStudentId
                justRun { tipTokens = 5 }
                justRun { tipTokensReplenishedAt = now }
                every { tipTokensReplenishedAt } returns lastReplenishDateMillis
                every { classroomId } returns testClassroomId
            }
            val students = listOf(student)
            val teacher = mockk<TeacherProfileEntity> {
                every { id } returns testTeacherId
                every { hasProSubscription(now) } returns false
                every { tipTokensReplenishedAt } returns lastReplenishDateMillis
            }
            val teachers = listOf(teacher)
            every { teacherProfileEntityRepository.findAll() } returns teachers
            every { teacherProfileEntityRepository.saveAll(teachers) } returns teachers
            every { studentProfileEntityRepository.findAll() } returns students
            every { studentProfileEntityRepository.saveAll(students) } returns students
            every { classroomService.listByIds(listOf(testClassroomId)) } returns listOf(mockk {
                every { id } returns testClassroomId
                every { teacherId } returns testTeacherId
            })
            every { clock.millis() } returns now
            tipTokensService.replenishTipTokens()
            verify(inverse = true) { student.tipTokens = 5 }
            verify(inverse = true) { student.tipTokensReplenishedAt = now }
        }



        @Test
        fun `should refresh tip tokens if student's teacher has standard subscription and free plan replenish period has not passed and current date between September 16 and October 18 2024`() {
            val lastReplenishDate = ZonedDateTime.of(2024, 9, 16, 0, 0, 0, 0, ZoneId.of("UTC-7"))
            val lastReplenishDateMillis = lastReplenishDate.toInstant().toEpochMilli()
            val now = lastReplenishDate
                .plusSeconds(1)
                .toInstant().toEpochMilli()

            val student = mockk<StudentProfileEntity> {
                every { id } returns testStudentId
                justRun { tipTokens = 5 }
                justRun { tipTokensReplenishedAt = now }
                every { tipTokensReplenishedAt } returns lastReplenishDateMillis
                every { classroomId } returns testClassroomId
            }
            val students = listOf(student)
            val teacher = mockk<TeacherProfileEntity> {
                every { id } returns testTeacherId
                every { hasProSubscription(now) } returns false
                every { tipTokensReplenishedAt } returns lastReplenishDateMillis
                justRun { tipTokens = 5 }
                justRun { tipTokensReplenishedAt = now }
            }
            val teachers = listOf(teacher)
            every { teacherProfileEntityRepository.findAll() } returns teachers
            every { teacherProfileEntityRepository.saveAll(teachers) } returns teachers
            every { studentProfileEntityRepository.findAll() } returns students
            every { studentProfileEntityRepository.saveAll(students) } returns students
            every { classroomService.listByIds(listOf(testClassroomId)) } returns listOf(mockk {
                every { id } returns testClassroomId
                every { teacherId } returns testTeacherId
            })
            every { clock.millis() } returns now
            tipTokensService.replenishTipTokens()
            verify { student.tipTokens = 5 }
            verify { student.tipTokensReplenishedAt = now }
        }
    }

    @DisplayName("When withdraw one tip tokens")
    @Nested
    inner class WithdrawOneTipToken {

        @Test
        fun `shouldn't withdraw one tip token when student not found`() {
            every { studentProfileEntityRepository.findByIdOrNull(testStudentId) } returns null
            tipTokensService.withdrawOneTipToken(studentProfile)
            verify(exactly = 0) { studentProfileEntityRepository.save(allAny()) }
        }

        @Test
        fun `shouldn't withdraw one tip token when teacher not found`() {
            every { teacherProfileEntityRepository.findByIdOrNull(testTeacherId) } returns null
            tipTokensService.withdrawOneTipToken(teacherProfile)
            verify(exactly = 0) { teacherProfileEntityRepository.save(allAny()) }
        }

        @Test
        fun `should withdraw one tip token when student exists`() {
            val entity = mockk<StudentProfileEntity> {
                every { tipTokens } returns 3
            }
            every { studentProfileEntityRepository.findByIdOrNull(testStudentId) } returns entity
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
            every { teacherProfileEntityRepository.findByIdOrNull(testTeacherId) } returns entity
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
            every { studentProfileEntityRepository.findByIdOrNull(testStudentId) } returns null
            assertNull(tipTokensService.hasTipTokens(studentProfile))
        }

        @Test
        fun `should return null when teacher not found`() {
            every { teacherProfileEntityRepository.findByIdOrNull(testTeacherId) } returns null
            assertNull(tipTokensService.hasTipTokens(teacherProfile))
        }

        @Test
        fun `should return false when student do not have tip tokens`() {
            every { studentProfileEntityRepository.findByIdOrNull(testStudentId) } returns mockk {
                every { tipTokens } returns 0
            }
            assertFalse(tipTokensService.hasTipTokens(studentProfile)!!)
        }

        @Test
        fun `should return false when teacher do not have tip tokens`() {
            every { teacherProfileEntityRepository.findByIdOrNull(testTeacherId) } returns mockk {
                every { tipTokens } returns 0
            }
            assertFalse(tipTokensService.hasTipTokens(teacherProfile)!!)
        }

        @Test
        fun `should return true when student has tip tokens`() {
            every { studentProfileEntityRepository.findByIdOrNull(testStudentId) } returns mockk {
                every { tipTokens } returns 2
            }
            assertTrue(tipTokensService.hasTipTokens(studentProfile)!!)
        }

        @Test
        fun `should return true when teacher has tip tokens`() {
            every { teacherProfileEntityRepository.findByIdOrNull(testTeacherId) } returns mockk {
                every { tipTokens } returns 2
            }
            assertTrue(tipTokensService.hasTipTokens(teacherProfile)!!)
        }
    }
}
