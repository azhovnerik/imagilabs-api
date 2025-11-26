package com.anahoret.imagilabsapi.teachers.domain

import arrow.core.left
import arrow.core.right
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

    private val useCase = TeacherProfileUpdateUseCaseImpl(
        teacherProfileEntityRepository,
        teacherProfileService
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
    fun `should update subjects as text field`() {
        val teacherId = UUID.randomUUID()
        val request = TeacherProfileUpdateRequest(
            subjects = "Mathematics, Computer Science"
        )

        val teacherEntity = createMockTeacherEntity(teacherId)
        every { teacherProfileEntityRepository.findByIdOrNull(teacherId) } returns teacherEntity
        every { teacherProfileEntityRepository.save(any()) } returns teacherEntity
        every { teacherProfileService.getTeacherById(teacherId) } returns mockk()

        val result = useCase.update(teacherId, request)

        assertTrue(result.isRight())
        assertEquals("Mathematics, Computer Science", teacherEntity.subjects)
    }

    @Test
    fun `should update schools as text field`() {
        val teacherId = UUID.randomUUID()
        val request = TeacherProfileUpdateRequest(schools = "Lincoln Elementary, Washington High")

        val teacherEntity = createMockTeacherEntity(teacherId)
        every { teacherProfileEntityRepository.findByIdOrNull(teacherId) } returns teacherEntity
        every { teacherProfileEntityRepository.save(any()) } returns teacherEntity
        every { teacherProfileService.getTeacherById(teacherId) } returns mockk()

        val result = useCase.update(teacherId, request)

        assertTrue(result.isRight())
        assertEquals("Lincoln Elementary, Washington High", teacherEntity.schools)
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
        }
    }
}