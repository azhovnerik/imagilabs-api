package com.anahoret.imagilabsapi.teachers.domain

import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscription
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionService
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntity
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntityRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Clock
import java.util.*

@DisplayName("Teacher profile service")
class TeacherProfileServiceTest {

    private val teacherProfileEntityRepository = mockk<TeacherProfileEntityRepository>()
    private val passwordEncoder = mockk<PasswordEncoder>()
    private val teacherSubscriptionService = mockk<TeacherSubscriptionService>()
    private val clock = mockk<Clock>()
    private val tipTokens = 100

    private val service = TeacherProfileServiceImpl(
        teacherProfileEntityRepository,
        passwordEncoder,
        teacherSubscriptionService,
        clock,
        tipTokens
    )

    @Test
    fun `updateProfile should return null when teacher not found`() {
        val teacherId = UUID.randomUUID()
        val request = TeacherProfileUpdateRequest(firstName = "John")

        every { teacherProfileEntityRepository.findByIdOrNull(teacherId) } returns null

        val result = service.updateProfile(teacherId, request)

        assertNull(result)
        verify { teacherProfileEntityRepository.findByIdOrNull(teacherId) }
    }

    @Test
    fun `updateProfile should update only non-null fields and preserve null fields`() {
        val teacherId = UUID.randomUUID()
        val originalEntity = createTeacherEntity(
            id = teacherId,
            firstName = "Original",
            lastName = "Name",
            country = "USA",
            state = "California",
            organization = "Original Org",
            marketingEmailSubscribed = false,
            schoolRoles = listOf(SchoolRole.TEACHER),
            grades = listOf(GradeLevel.FIRST_GRADE),
            subjects = "Math",
            schools = "School A"
        )

        val request = TeacherProfileUpdateRequest(
            firstName = "Updated",
            lastName = null, // should remain unchanged
            country = "Canada",
            state = null, // should remain unchanged
            organization = null, // should remain unchanged
            marketingEmailSubscribed = true,
            schoolRoles = listOf(SchoolRole.COORDINATOR),
            grades = null, // should remain unchanged
            subjects = null, // should remain unchanged
            schools = "School B"
        )

        val savedEntitySlot = slot<TeacherProfileEntity>()
        val mockSubscription = mockk<TeacherSubscription>()

        every { teacherProfileEntityRepository.findByIdOrNull(teacherId) } returns originalEntity
        every { teacherProfileEntityRepository.save(capture(savedEntitySlot)) } answers { savedEntitySlot.captured }
        every { teacherSubscriptionService.buildSubscriptionDto(teacherId, any()) } returns mockSubscription

        val result = service.updateProfile(teacherId, request)

        assertNotNull(result)

        // Verify that the entity was saved with correct values
        val savedEntity = savedEntitySlot.captured

        // Updated fields (non-null in request)
        assertEquals("Updated", savedEntity.firstName)
        assertEquals("Canada", savedEntity.country)
        assertEquals(true, savedEntity.marketingEmailSubscribed)
        assertEquals(listOf(SchoolRole.COORDINATOR), savedEntity.schoolRoles)
        assertEquals("School B", savedEntity.schools)

        // Unchanged fields (null in request)
        assertEquals("Name", savedEntity.lastName)
        assertEquals("California", savedEntity.state)
        assertEquals("Original Org", savedEntity.organization)
        assertEquals(listOf(GradeLevel.FIRST_GRADE), savedEntity.grades)
        assertEquals("Math", savedEntity.subjects)

        verify { teacherProfileEntityRepository.findByIdOrNull(teacherId) }
        verify { teacherProfileEntityRepository.save(any()) }
        verify { teacherSubscriptionService.buildSubscriptionDto(teacherId, savedEntity) }
    }

    private fun createTeacherEntity(
        id: UUID,
        firstName: String = "Test",
        lastName: String = "Teacher",
        country: String = "USA",
        state: String? = null,
        organization: String = "Test Org",
        marketingEmailSubscribed: Boolean = false,
        schoolRoles: List<SchoolRole> = emptyList(),
        grades: List<GradeLevel> = emptyList(),
        subjects: String? = null,
        schools: String? = null
    ): TeacherProfileEntity {
        return TeacherProfileEntity(
            email = "test@test.com",
            passwordHash = "hash",
            firstName = firstName,
            lastName = lastName,
            country = country,
            organization = organization,
            howDidYouHearAboutUs = "Test",
            howDidYouHearAboutUsOther = null,
            marketingEmailSubscribed = marketingEmailSubscribed,
            tipTokens = 10,
            tipTokensReplenishedAt = 0L,
            state = state,
            schoolRoles = schoolRoles,
            grades = grades,
            subjects = subjects,
            schools = schools
        ).apply {
            this.id = id
        }
    }
}
