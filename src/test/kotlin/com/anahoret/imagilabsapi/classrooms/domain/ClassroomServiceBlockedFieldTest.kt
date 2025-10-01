package com.anahoret.imagilabsapi.classrooms.domain

import com.anahoret.imagilabsapi.classrooms.storage.ClassroomEntity
import com.anahoret.imagilabsapi.classrooms.storage.ClassroomEntityRepository
import com.anahoret.imagilabsapi.coteachers.domain.CoTeacherService
import com.anahoret.imagilabsapi.projectclassroomshare.domain.ProjectClassroomShareService
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscription
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionPlan
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionService
import com.anahoret.imagilabsapi.userclassroomlink.domain.StudentClassroomLinkService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.*

@DisplayName("Classroom service blocked field tests")
class ClassroomServiceBlockedFieldTest {

    private val classroomEntityRepository = mockk<ClassroomEntityRepository>()
    private val studentClassroomLinkService = mockk<StudentClassroomLinkService>()
    private val projectClassroomShareService = mockk<ProjectClassroomShareService>()
    private val coTeacherService = mockk<CoTeacherService>()
    private val teacherSubscriptionService = mockk<TeacherSubscriptionService>()
    private val clock = Clock.fixed(Instant.ofEpochMilli(500L), ZoneId.systemDefault())

    private val classroomService = ClassroomServiceImpl(
        classroomEntityRepository,
        studentClassroomLinkService,
        projectClassroomShareService,
        coTeacherService,
        teacherSubscriptionService,
        clock
    )

    @Test
    fun `first classroom should not be blocked even with canceled subscription`() {
        val teacherId = UUID.randomUUID()
        val classroomId = UUID.randomUUID()
        val classroom = ClassroomEntity("Test Classroom", "ABC123", teacherId).apply {
            id = classroomId
            createdAt = 100L
        }

        val subscription = TeacherSubscription(
            start = 50L,
            end = 200L,
            plan = TeacherSubscriptionPlan.STANDARD,
            canceled = true,
            teacherId = teacherId
        )

        every { classroomEntityRepository.findByIdOrNull(classroomId) } returns classroom
        every { studentClassroomLinkService.getStudentCount(classroomId) } returns 0
        every { projectClassroomShareService.getProjectCount(classroomId) } returns 0
        every { coTeacherService.getCoTeacherCountByClassroomId(classroomId) } returns 0
        every { classroomEntityRepository.findAllByTeacherId(teacherId) } returns listOf(classroom)
        every { teacherSubscriptionService.getSubscriptionDto(teacherId) } returns subscription

        val result = classroomService.getById(classroomId)

        assertNotNull(result)
        assertFalse(result!!.blocked, "First classroom should not be blocked")
    }

    @Test
    fun `second classroom should be blocked when subscription is canceled`() {
        val teacherId = UUID.randomUUID()
        val firstClassroomId = UUID.randomUUID()
        val secondClassroomId = UUID.randomUUID()

        val firstClassroom = ClassroomEntity("First Classroom", "ABC123", teacherId).apply {
            id = firstClassroomId
            createdAt = 200L
        }

        val secondClassroom = ClassroomEntity("Second Classroom", "DEF456", teacherId).apply {
            id = secondClassroomId
            createdAt = 300L
        }

        val subscription = TeacherSubscription(
            start = 100L,
            end = 600L,
            plan = TeacherSubscriptionPlan.STANDARD,
            canceled = true,
            teacherId = teacherId
        )

        every { classroomEntityRepository.findByIdOrNull(secondClassroomId) } returns secondClassroom
        every { studentClassroomLinkService.getStudentCount(secondClassroomId) } returns 0
        every { projectClassroomShareService.getProjectCount(secondClassroomId) } returns 0
        every { coTeacherService.getCoTeacherCountByClassroomId(secondClassroomId) } returns 0
        every { classroomEntityRepository.findAllByTeacherId(teacherId) } returns listOf(
            firstClassroom,
            secondClassroom
        )
        every { teacherSubscriptionService.getSubscriptionDto(teacherId) } returns subscription

        val result = classroomService.getById(secondClassroomId)

        assertNotNull(result)
        assertTrue(result!!.blocked, "Second classroom should be blocked when subscription is canceled")
    }

    @Test
    fun `second classroom should be blocked when subscription has ended`() {
        val teacherId = UUID.randomUUID()
        val firstClassroomId = UUID.randomUUID()
        val secondClassroomId = UUID.randomUUID()

        val firstClassroom = ClassroomEntity("First Classroom", "ABC123", teacherId).apply {
            id = firstClassroomId
            createdAt = 200L
        }

        val secondClassroom = ClassroomEntity("Second Classroom", "DEF456", teacherId).apply {
            id = secondClassroomId
            createdAt = 300L
        }

        val subscription = TeacherSubscription(
            start = 100L,
            end = 400L, // Subscription ended before current time (500L)
            plan = TeacherSubscriptionPlan.PRO,
            canceled = false,
            teacherId = teacherId
        )

        every { classroomEntityRepository.findByIdOrNull(secondClassroomId) } returns secondClassroom
        every { studentClassroomLinkService.getStudentCount(secondClassroomId) } returns 0
        every { projectClassroomShareService.getProjectCount(secondClassroomId) } returns 0
        every { coTeacherService.getCoTeacherCountByClassroomId(secondClassroomId) } returns 0
        every { classroomEntityRepository.findAllByTeacherId(teacherId) } returns listOf(
            firstClassroom,
            secondClassroom
        )
        every { teacherSubscriptionService.getSubscriptionDto(teacherId) } returns subscription

        val result = classroomService.getById(secondClassroomId)

        assertNotNull(result)
        assertTrue(result!!.blocked, "Second classroom should be blocked when subscription has ended")
    }

    @Test
    fun `second classroom should not be blocked with active subscription`() {
        val teacherId = UUID.randomUUID()
        val firstClassroomId = UUID.randomUUID()
        val secondClassroomId = UUID.randomUUID()

        val firstClassroom = ClassroomEntity("First Classroom", "ABC123", teacherId).apply {
            id = firstClassroomId
            createdAt = 200L
        }

        val secondClassroom = ClassroomEntity("Second Classroom", "DEF456", teacherId).apply {
            id = secondClassroomId
            createdAt = 300L
        }

        val subscription = TeacherSubscription(
            start = 100L,
            end = 800L, // Subscription is still active (current time is 500L)
            plan = TeacherSubscriptionPlan.PRO,
            canceled = false,
            teacherId = teacherId
        )

        every { classroomEntityRepository.findByIdOrNull(secondClassroomId) } returns secondClassroom
        every { studentClassroomLinkService.getStudentCount(secondClassroomId) } returns 0
        every { projectClassroomShareService.getProjectCount(secondClassroomId) } returns 0
        every { coTeacherService.getCoTeacherCountByClassroomId(secondClassroomId) } returns 0
        every { classroomEntityRepository.findAllByTeacherId(teacherId) } returns listOf(
            firstClassroom,
            secondClassroom
        )
        every { teacherSubscriptionService.getSubscriptionDto(teacherId) } returns subscription

        val result = classroomService.getById(secondClassroomId)

        assertNotNull(result)
        assertFalse(result!!.blocked, "Second classroom should not be blocked with active subscription")
    }

    @Test
    fun `classrooms should not be blocked with no subscription data`() {
        val teacherId = UUID.randomUUID()
        val classroomId = UUID.randomUUID()

        val classroom = ClassroomEntity("Test Classroom", "ABC123", teacherId).apply {
            id = classroomId
            createdAt = 200L
        }

        every { classroomEntityRepository.findByIdOrNull(classroomId) } returns classroom
        every { studentClassroomLinkService.getStudentCount(classroomId) } returns 0
        every { projectClassroomShareService.getProjectCount(classroomId) } returns 0
        every { coTeacherService.getCoTeacherCountByClassroomId(classroomId) } returns 0
        every { classroomEntityRepository.findAllByTeacherId(teacherId) } returns listOf(classroom)
        every { teacherSubscriptionService.getSubscriptionDto(teacherId) } returns null

        val result = classroomService.getById(classroomId)

        assertNotNull(result)
        assertFalse(result!!.blocked, "Classroom should not be blocked when subscription data is unavailable")
    }

    @Test
    fun `list by teacher should correctly calculate blocked field for multiple classrooms`() {
        val teacherId = UUID.randomUUID()
        val firstClassroomId = UUID.randomUUID()
        val secondClassroomId = UUID.randomUUID()
        val thirdClassroomId = UUID.randomUUID()

        val firstClassroom = ClassroomEntity("First Classroom", "ABC123", teacherId).apply {
            id = firstClassroomId
            createdAt = 100L
        }

        val secondClassroom = ClassroomEntity("Second Classroom", "DEF456", teacherId).apply {
            id = secondClassroomId
            createdAt = 300L
        }

        val thirdClassroom = ClassroomEntity("Third Classroom", "GHI789", teacherId).apply {
            id = thirdClassroomId
            createdAt = 400L
        }

        val subscription = TeacherSubscription(
            start = 50L,
            end = 600L,
            plan = TeacherSubscriptionPlan.STANDARD,
            canceled = true,
            teacherId = teacherId
        )

        every { classroomEntityRepository.findAllByTeacherId(teacherId) } returns listOf(
            firstClassroom,
            secondClassroom,
            thirdClassroom
        )
        every { studentClassroomLinkService.getStudentCounts(any()) } returns emptyMap()
        every { projectClassroomShareService.getProjectCountsByClassrooms(any()) } returns emptyMap()
        every { coTeacherService.getCoTeacherCountsByClassroomIds(any()) } returns emptyMap()
        every { classroomEntityRepository.findAllByTeacherIdIn(setOf(teacherId)) } returns listOf(
            firstClassroom,
            secondClassroom,
            thirdClassroom
        )
        every { teacherSubscriptionService.getSubscriptionDtos(setOf(teacherId)) } returns listOf(subscription)

        val results = classroomService.listByTeacher(teacherId)

        assertEquals(3, results.size)
        val firstResult = results.find { it.id == firstClassroomId }
        val secondResult = results.find { it.id == secondClassroomId }
        val thirdResult = results.find { it.id == thirdClassroomId }

        assertFalse(firstResult!!.blocked, "First classroom should not be blocked")
        assertTrue(secondResult!!.blocked, "Second classroom should be blocked")
        assertTrue(thirdResult!!.blocked, "Third classroom should be blocked")
    }
}
