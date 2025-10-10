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
class ClassroomServiceImplTest {

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
        every { teacherSubscriptionService.getSubscriptionDtos(setOf(teacherId)) } returns listOf(subscription)

        val result = classroomService.getById(classroomId)

        assertNotNull(result)
        assertFalse(result!!.blocked, "First classroom should not be blocked")
    }

    @Test
    fun `second classroom should not be blocked when subscription is canceled but teacher has pro subscription`() {
        val testTeacherId = UUID.randomUUID()
        val firstClassroomId = UUID.randomUUID()
        val secondClassroomId = UUID.randomUUID()

        val firstClassroom = ClassroomEntity("First Classroom", "ABC123", testTeacherId).apply {
            id = firstClassroomId
            createdAt = 200L
        }

        val secondClassroom = ClassroomEntity("Second Classroom", "DEF456", testTeacherId).apply {
            id = secondClassroomId
            createdAt = 300L
        }

        val subscription = mockk<TeacherSubscription> {
            every { teacherId } returns testTeacherId
            every { hasProSubscription(500L) } returns true
        }

        every { classroomEntityRepository.findByIdOrNull(secondClassroomId) } returns secondClassroom
        every { studentClassroomLinkService.getStudentCount(secondClassroomId) } returns 0
        every { projectClassroomShareService.getProjectCount(secondClassroomId) } returns 0
        every { coTeacherService.getCoTeacherCountByClassroomId(secondClassroomId) } returns 0
        every { classroomEntityRepository.findAllByTeacherId(testTeacherId) } returns listOf(
            firstClassroom,
            secondClassroom
        )
        every { teacherSubscriptionService.getSubscriptionDto(testTeacherId) } returns subscription
        every { teacherSubscriptionService.getSubscriptionDtos(setOf(testTeacherId)) } returns listOf(subscription)

        val result = classroomService.getById(secondClassroomId)

        assertNotNull(result)
        assertFalse(result!!.blocked, "Second classroom should not be blocked")
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
        every { teacherSubscriptionService.getSubscriptionDtos(setOf(teacherId)) } returns listOf(subscription)

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
        every { teacherSubscriptionService.getSubscriptionDtos(setOf(teacherId)) } returns listOf(subscription)

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
        every { teacherSubscriptionService.getSubscriptionDtos(setOf(teacherId)) } returns emptyList()

        val result = classroomService.getById(classroomId)

        assertNotNull(result)
        assertFalse(result!!.blocked, "Classroom should not be blocked when subscription data is unavailable")
    }

    @Test
    fun `list by teacher should correctly calculate blocked field for multiple classrooms`() {
        val testTeacherId = UUID.randomUUID()
        val firstClassroomId = UUID.randomUUID()
        val secondClassroomId = UUID.randomUUID()
        val thirdClassroomId = UUID.randomUUID()

        val firstClassroom = ClassroomEntity("First Classroom", "ABC123", testTeacherId).apply {
            id = firstClassroomId
            createdAt = 100L
        }

        val secondClassroom = ClassroomEntity("Second Classroom", "DEF456", testTeacherId).apply {
            id = secondClassroomId
            createdAt = 300L
        }

        val thirdClassroom = ClassroomEntity("Third Classroom", "GHI789", testTeacherId).apply {
            id = thirdClassroomId
            createdAt = 400L
        }

        val subscription = mockk<TeacherSubscription> {
            every { teacherId } returns testTeacherId
            every { hasProSubscription(500L) } returns false
        }

        every { classroomEntityRepository.findAllByTeacherId(testTeacherId) } returns listOf(
            firstClassroom,
            secondClassroom,
            thirdClassroom
        )
        every { studentClassroomLinkService.getStudentCounts(any()) } returns emptyMap()
        every { projectClassroomShareService.getProjectCountsByClassrooms(any()) } returns emptyMap()
        every { coTeacherService.getCoTeacherCountsByClassroomIds(any()) } returns emptyMap()
        every { classroomEntityRepository.findAllByTeacherIdIn(setOf(testTeacherId)) } returns listOf(
            firstClassroom,
            secondClassroom,
            thirdClassroom
        )
        every { teacherSubscriptionService.getSubscriptionDtos(setOf(testTeacherId)) } returns listOf(subscription)

        val results = classroomService.listByTeacher(testTeacherId)

        assertEquals(3, results.size)
        val firstResult = results.find { it.id == firstClassroomId }
        val secondResult = results.find { it.id == secondClassroomId }
        val thirdResult = results.find { it.id == thirdClassroomId }

        assertFalse(firstResult!!.blocked, "First classroom should not be blocked")
        assertTrue(secondResult!!.blocked, "Second classroom should be blocked")
        assertTrue(thirdResult!!.blocked, "Third classroom should be blocked")
    }

    @Test
    fun `co-teacher should see classroom blocked when owner has no pro subscription even if first classroom`() {
        val ownerTeacherId = UUID.randomUUID()
        val classroomId = UUID.randomUUID()

        val firstClassroom = ClassroomEntity("Owner First Classroom", "ABC123", ownerTeacherId).apply {
            id = classroomId
            createdAt = 100L
        }

        val subscription = mockk<TeacherSubscription> {
            every { teacherId } returns ownerTeacherId
            every { hasProSubscription(500L) } returns false
        }

        every { classroomEntityRepository.findAllById(listOf(classroomId)) } returns listOf(firstClassroom)
        every { studentClassroomLinkService.getStudentCounts(any()) } returns emptyMap()
        every { projectClassroomShareService.getProjectCountsByClassrooms(any()) } returns emptyMap()
        every { coTeacherService.getCoTeacherCountsByClassroomIds(any()) } returns emptyMap()
        every { classroomEntityRepository.findAllByTeacherIdIn(setOf(ownerTeacherId)) } returns listOf(firstClassroom)
        every { teacherSubscriptionService.getSubscriptionDtos(setOf(ownerTeacherId)) } returns listOf(subscription)

        val results = classroomService.getAllClassroomAsCoTeacher(listOf(classroomId))

        assertEquals(1, results.size)
        val classroom = results.first()
        assertTrue(classroom.blocked, "Co-teacher should see classroom blocked when owner has no pro subscription")
        assertFalse(
            classroom.permissions.canManageCoTeachers,
            "Co-teacher permissions should reflect owner's non-pro subscription"
        )
    }

    @Test
    fun `list by teacher should set canManageCoTeachers to true when owner has pro subscription`() {
        val teacherId = UUID.randomUUID()
        val classroom1 = ClassroomEntity("Class A", "CODEA", teacherId).apply {
            id = UUID.randomUUID()
            createdAt = 100L
        }
        val classroom2 = ClassroomEntity("Class B", "CODEB", teacherId).apply {
            id = UUID.randomUUID()
            createdAt = 200L
        }

        val subscription = mockk<TeacherSubscription>()
        every { subscription.teacherId } returns teacherId
        every { subscription.hasProSubscription(500L) } returns true

        every { classroomEntityRepository.findAllByTeacherId(teacherId) } returns listOf(classroom1, classroom2)
        every { classroomEntityRepository.findAllByTeacherIdIn(setOf(teacherId)) } returns listOf(
            classroom1,
            classroom2
        )
        every { studentClassroomLinkService.getStudentCounts(any()) } returns emptyMap()
        every { projectClassroomShareService.getProjectCountsByClassrooms(any()) } returns emptyMap()
        every { coTeacherService.getCoTeacherCountsByClassroomIds(any()) } returns emptyMap()
        every { teacherSubscriptionService.getSubscriptionDtos(setOf(teacherId)) } returns listOf(subscription)

        val results = classroomService.listByTeacher(teacherId)

        assertEquals(2, results.size)
        assertTrue(results.all { it.permissions.canManageCoTeachers })
    }

    @Test
    fun `list by teacher should set canManageCoTeachers to false when owner has no pro subscription`() {
        val teacherId = UUID.randomUUID()
        val classroom1 = ClassroomEntity("Class C", "CODEC", teacherId).apply {
            id = UUID.randomUUID()
            createdAt = 100L
        }
        val classroom2 = ClassroomEntity("Class D", "CODED", teacherId).apply {
            id = UUID.randomUUID()
            createdAt = 200L
        }

        val subscription = mockk<TeacherSubscription>()
        every { subscription.teacherId } returns teacherId
        every { subscription.hasProSubscription(500L) } returns false

        every { classroomEntityRepository.findAllByTeacherId(teacherId) } returns listOf(classroom1, classroom2)
        every { classroomEntityRepository.findAllByTeacherIdIn(setOf(teacherId)) } returns listOf(
            classroom1,
            classroom2
        )
        every { studentClassroomLinkService.getStudentCounts(any()) } returns emptyMap()
        every { projectClassroomShareService.getProjectCountsByClassrooms(any()) } returns emptyMap()
        every { coTeacherService.getCoTeacherCountsByClassroomIds(any()) } returns emptyMap()
        every { teacherSubscriptionService.getSubscriptionDtos(setOf(teacherId)) } returns listOf(subscription)

        val results = classroomService.listByTeacher(teacherId)

        assertEquals(2, results.size)
        assertTrue(results.all { !it.permissions.canManageCoTeachers })
    }

    @Test
    fun `get by id should set canManageCoTeachers based on owner's pro subscription`() {
        val teacherId = UUID.randomUUID()
        val classroomId = UUID.randomUUID()
        val classroom = ClassroomEntity("Single Class", "SINGLE1", teacherId).apply {
            id = classroomId
            createdAt = 150L
        }

        val subscription = mockk<TeacherSubscription>()
        every { subscription.teacherId } returns teacherId
        every { subscription.hasProSubscription(500L) } returns true

        every { classroomEntityRepository.findByIdOrNull(classroomId) } returns classroom
        every { studentClassroomLinkService.getStudentCount(classroomId) } returns 0
        every { projectClassroomShareService.getProjectCount(classroomId) } returns 0
        every { coTeacherService.getCoTeacherCountByClassroomId(classroomId) } returns 0
        every { classroomEntityRepository.findAllByTeacherId(teacherId) } returns listOf(classroom)
        every { teacherSubscriptionService.getSubscriptionDto(teacherId) } returns subscription
        every { teacherSubscriptionService.getSubscriptionDtos(setOf(teacherId)) } returns listOf(subscription)

        val result = classroomService.getById(classroomId)

        assertNotNull(result)
        assertTrue(result!!.permissions.canManageCoTeachers)
    }
}
