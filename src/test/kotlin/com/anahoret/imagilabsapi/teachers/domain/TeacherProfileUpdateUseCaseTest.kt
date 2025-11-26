package com.anahoret.imagilabsapi.teachers.domain

import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.schools.domain.SchoolService
import com.anahoret.imagilabsapi.schools.storage.SchoolEntity
import com.anahoret.imagilabsapi.schools.storage.SchoolRepository
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscription
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntity
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntityRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import java.util.*

@DisplayName("Teacher profile update use case")
class TeacherProfileUpdateUseCaseTest {

    private val teacherProfileEntityRepository = mockk<TeacherProfileEntityRepository>(relaxed = true)
    private val teacherProfileService = mockk<TeacherProfileService>()
    private val schoolService = mockk<SchoolService>()
    private val schoolRepository = mockk<SchoolRepository>()

    private val useCase = TeacherProfileUpdateUseCaseImpl(
        teacherProfileEntityRepository,
        teacherProfileService,
        schoolService,
        schoolRepository
    )

    @Test
    fun `should return teacher not found error when teacher does not exist`() {
        val teacherId = UUID.randomUUID()
        val request = TeacherProfileUpdateRequest(firstName = "John")

        every { teacherProfileEntityRepository.findByIdOrNull(teacherId) } returns null

        val result = useCase.update(teacherId, request)

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return validation error when school IDs are invalid`() {
        val teacherId = UUID.randomUUID()
        val invalidSchoolId = UUID.randomUUID()
        val request = TeacherProfileUpdateRequest(schoolIds = listOf(invalidSchoolId))

        val teacherEntity = createMockTeacherEntity(teacherId)
        every { teacherProfileEntityRepository.findByIdOrNull(teacherId) } returns teacherEntity
        every { schoolRepository.existsById(invalidSchoolId) } returns false

        val result = useCase.update(teacherId, request)

        assertTrue(result.isLeft())
    }

    @Test
    fun `should update only provided fields`() {
        val teacherId = UUID.randomUUID()
        val request = TeacherProfileUpdateRequest(
            firstName = "John",
            lastName = "Doe",
            state = "California"
        )

        val teacherEntity = createMockTeacherEntity(teacherId)
        every { teacherProfileEntityRepository.findByIdOrNull(teacherId) } returns teacherEntity
        every { teacherProfileEntityRepository.save(any()) } returns teacherEntity
        every { teacherProfileService.getTeacherById(teacherId) } returns mockk()

        val result = useCase.update(teacherId, request)

        assertTrue(result.isRight())
        assertEquals("John", teacherEntity.firstName)
        assertEquals("Doe", teacherEntity.lastName)
        assertEquals("California", teacherEntity.state)
    }

    @Test
    fun `should update school roles as comma-separated string`() {
        val teacherId = UUID.randomUUID()
        val request = TeacherProfileUpdateRequest(
            schoolRoles = listOf(SchoolRole.TEACHER, SchoolRole.COORDINATOR)
        )

        val teacherEntity = createMockTeacherEntity(teacherId)
        every { teacherProfileEntityRepository.findByIdOrNull(teacherId) } returns teacherEntity
        every { teacherProfileEntityRepository.save(any()) } returns teacherEntity
        every { teacherProfileService.getTeacherById(teacherId) } returns mockk()

        val result = useCase.update(teacherId, request)

        assertTrue(result.isRight())
        assertEquals("TEACHER,COORDINATOR", teacherEntity.schoolRoles)
    }

    @Test
    fun `should update grades as comma-separated string`() {
        val teacherId = UUID.randomUUID()
        val request = TeacherProfileUpdateRequest(
            grades = listOf(GradeLevel.FIRST_GRADE, GradeLevel.SECOND_GRADE)
        )

        val teacherEntity = createMockTeacherEntity(teacherId)
        every { teacherProfileEntityRepository.findByIdOrNull(teacherId) } returns teacherEntity
        every { teacherProfileEntityRepository.save(any()) } returns teacherEntity
        every { teacherProfileService.getTeacherById(teacherId) } returns mockk()

        val result = useCase.update(teacherId, request)

        assertTrue(result.isRight())
        assertEquals("FIRST_GRADE,SECOND_GRADE", teacherEntity.grades)
    }

    @Test
    fun `should update subjects as comma-separated string`() {
        val teacherId = UUID.randomUUID()
        val request = TeacherProfileUpdateRequest(
            subjects = listOf(Subject.MATHEMATICS, Subject.COMPUTER_SCIENCE)
        )

        val teacherEntity = createMockTeacherEntity(teacherId)
        every { teacherProfileEntityRepository.findByIdOrNull(teacherId) } returns teacherEntity
        every { teacherProfileEntityRepository.save(any()) } returns teacherEntity
        every { teacherProfileService.getTeacherById(teacherId) } returns mockk()

        val result = useCase.update(teacherId, request)

        assertTrue(result.isRight())
        assertEquals("MATHEMATICS,COMPUTER_SCIENCE", teacherEntity.subjects)
    }

    @Test
    fun `should replace schools with full replacement strategy`() {
        val teacherId = UUID.randomUUID()
        val oldSchoolId = UUID.randomUUID()
        val schoolId1 = UUID.randomUUID()
        val schoolId2 = UUID.randomUUID()
        val request = TeacherProfileUpdateRequest(schoolIds = listOf(schoolId1, schoolId2))

        val teacherEntity = createMockTeacherEntity(teacherId)
        val oldSchool = SchoolEntity("Old School").apply { id = oldSchoolId }
        teacherEntity.schools.add(oldSchool)

        val school1 = SchoolEntity("School 1").apply { id = schoolId1 }
        val school2 = SchoolEntity("School 2").apply { id = schoolId2 }

        every { teacherProfileEntityRepository.findByIdOrNull(teacherId) } returns teacherEntity
        every { schoolRepository.existsById(schoolId1) } returns true
        every { schoolRepository.existsById(schoolId2) } returns true
        every { schoolRepository.findAllById(listOf(schoolId1, schoolId2)) } returns listOf(school1, school2)
        every { teacherProfileEntityRepository.save(any()) } returns teacherEntity
        every { teacherProfileService.getTeacherById(teacherId) } returns mockk()

        val result = useCase.update(teacherId, request)

        assertTrue(result.isRight())
        assertEquals(2, teacherEntity.schools.size)
        assertFalse(teacherEntity.schools.contains(oldSchool))
        assertTrue(teacherEntity.schools.contains(school1))
        assertTrue(teacherEntity.schools.contains(school2))
    }

    @Test
    fun `should not update fields when they are null`() {
        val teacherId = UUID.randomUUID()
        val request = TeacherProfileUpdateRequest(firstName = null, lastName = null)

        val teacherEntity = createMockTeacherEntity(teacherId)
        val originalFirstName = teacherEntity.firstName
        val originalLastName = teacherEntity.lastName

        every { teacherProfileEntityRepository.findByIdOrNull(teacherId) } returns teacherEntity
        every { teacherProfileEntityRepository.save(any()) } returns teacherEntity
        every { teacherProfileService.getTeacherById(teacherId) } returns mockk()

        val result = useCase.update(teacherId, request)

        assertTrue(result.isRight())
        assertEquals(originalFirstName, teacherEntity.firstName)
        assertEquals(originalLastName, teacherEntity.lastName)
    }

    private fun createMockTeacherEntity(id: UUID): TeacherProfileEntity {
        return TeacherProfileEntity(
            email = "test@test.com",
            passwordHash = "hash",
            firstName = "Original",
            lastName = "Name",
            country = "USA",
            organization = "Test Org",
            howDidYouHearAboutUs = "Test",
            howDidYouHearAboutUsOther = null,
            marketingEmailSubscribed = false,
            tipTokens = 10,
            tipTokensReplenishedAt = 0L
        ).apply {
            this.id = id
            this.schools = mutableSetOf()
        }
    }
}