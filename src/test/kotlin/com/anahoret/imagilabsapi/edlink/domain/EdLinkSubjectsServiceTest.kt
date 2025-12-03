package com.anahoret.imagilabsapi.edlink.domain

import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.edlink.api.EdLinkClassApi
import com.anahoret.imagilabsapi.edlink.api.EdLinkEnrollmentApi
import com.anahoret.imagilabsapi.edlink.api.EdLinkSubjectApi
import com.anahoret.imagilabsapi.edlink.api.model.EdLinkClass
import com.anahoret.imagilabsapi.edlink.api.model.Enrollment
import com.anahoret.imagilabsapi.edlink.api.model.Subject
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("EdLink Subjects Service")
class EdLinkSubjectsServiceTest {

    private val edLinkEnrollmentApi = mockk<EdLinkEnrollmentApi>()
    private val edLinkClassApi = mockk<EdLinkClassApi>()
    private val edLinkSubjectApi = mockk<EdLinkSubjectApi>()

    private val service = EdLinkSubjectsService(
        edLinkEnrollmentApi,
        edLinkClassApi,
        edLinkSubjectApi
    )

    private val token = "test-token"
    private val personId = UUID.randomUUID()

    @Test
    fun `should return subjects when all data is available`() {
        val classId1 = UUID.randomUUID()
        val classId2 = UUID.randomUUID()
        val subjectId1 = UUID.randomUUID()
        val subjectId2 = UUID.randomUUID()

        val enrollments = listOf(
            createEnrollment(classId = classId1),
            createEnrollment(classId = classId2)
        )
        val classes = listOf(
            createClass(id = classId1, subjectId = subjectId1),
            createClass(id = classId2, subjectId = subjectId2)
        )
        val subjects = listOf(
            createSubject(id = subjectId1, name = "Mathematics"),
            createSubject(id = subjectId2, name = "Science")
        )

        every { edLinkEnrollmentApi.listTeacherEnrollments(token, personId) } returns enrollments.right()
        every { edLinkClassApi.listClasses(token, listOf(classId1, classId2)) } returns classes.right()
        every { edLinkSubjectApi.listSubjects(token, listOf(subjectId1, subjectId2)) } returns subjects.right()

        val result = service.listTeacherSubjects(token, personId)

        assertEquals("Mathematics, Science", result.getOrNull())
        verify(exactly = 1) { edLinkEnrollmentApi.listTeacherEnrollments(token, personId) }
        verify(exactly = 1) { edLinkClassApi.listClasses(token, listOf(classId1, classId2)) }
        verify(exactly = 1) { edLinkSubjectApi.listSubjects(token, listOf(subjectId1, subjectId2)) }
    }

    @Test
    fun `should deduplicate subject names`() {
        val classId1 = UUID.randomUUID()
        val classId2 = UUID.randomUUID()
        val subjectId = UUID.randomUUID()

        val enrollments = listOf(
            createEnrollment(classId = classId1),
            createEnrollment(classId = classId2)
        )
        val classes = listOf(
            createClass(id = classId1, subjectId = subjectId),
            createClass(id = classId2, subjectId = subjectId)
        )
        val subjects = listOf(
            createSubject(id = subjectId, name = "Mathematics")
        )

        every { edLinkEnrollmentApi.listTeacherEnrollments(token, personId) } returns enrollments.right()
        every { edLinkClassApi.listClasses(token, listOf(classId1, classId2)) } returns classes.right()
        every { edLinkSubjectApi.listSubjects(token, listOf(subjectId)) } returns subjects.right()

        val result = service.listTeacherSubjects(token, personId)

        assertEquals("Mathematics", result.getOrNull())
    }

    @Test
    fun `should sort subject names alphabetically`() {
        val classId1 = UUID.randomUUID()
        val classId2 = UUID.randomUUID()
        val classId3 = UUID.randomUUID()
        val subjectId1 = UUID.randomUUID()
        val subjectId2 = UUID.randomUUID()
        val subjectId3 = UUID.randomUUID()

        val enrollments = listOf(
            createEnrollment(classId = classId1),
            createEnrollment(classId = classId2),
            createEnrollment(classId = classId3)
        )
        val classes = listOf(
            createClass(id = classId1, subjectId = subjectId1),
            createClass(id = classId2, subjectId = subjectId2),
            createClass(id = classId3, subjectId = subjectId3)
        )
        val subjects = listOf(
            createSubject(id = subjectId1, name = "Zebra Science"),
            createSubject(id = subjectId2, name = "Art"),
            createSubject(id = subjectId3, name = "Music")
        )

        every { edLinkEnrollmentApi.listTeacherEnrollments(token, personId) } returns enrollments.right()
        every { edLinkClassApi.listClasses(token, any()) } returns classes.right()
        every { edLinkSubjectApi.listSubjects(token, any()) } returns subjects.right()

        val result = service.listTeacherSubjects(token, personId)

        assertEquals("Art, Music, Zebra Science", result.getOrNull())
    }

    @Test
    fun `should return null when no enrollments found`() {
        every { edLinkEnrollmentApi.listTeacherEnrollments(token, personId) } returns emptyList<Enrollment>().right()

        val result = service.listTeacherSubjects(token, personId)

        assertNull(result.getOrNull())
        verify(exactly = 1) { edLinkEnrollmentApi.listTeacherEnrollments(token, personId) }
        verify(exactly = 0) { edLinkClassApi.listClasses(any(), any()) }
        verify(exactly = 0) { edLinkSubjectApi.listSubjects(any(), any()) }
    }

    @Test
    fun `should return null when classes have no subject_id`() {
        val classId1 = UUID.randomUUID()
        val classId2 = UUID.randomUUID()

        val enrollments = listOf(
            createEnrollment(classId = classId1),
            createEnrollment(classId = classId2)
        )
        val classes = listOf(
            createClass(id = classId1, subjectId = null),
            createClass(id = classId2, subjectId = null)
        )

        every { edLinkEnrollmentApi.listTeacherEnrollments(token, personId) } returns enrollments.right()
        every { edLinkClassApi.listClasses(token, listOf(classId1, classId2)) } returns classes.right()

        val result = service.listTeacherSubjects(token, personId)

        assertNull(result.getOrNull())
        verify(exactly = 1) { edLinkEnrollmentApi.listTeacherEnrollments(token, personId) }
        verify(exactly = 1) { edLinkClassApi.listClasses(token, listOf(classId1, classId2)) }
        verify(exactly = 0) { edLinkSubjectApi.listSubjects(any(), any()) }
    }

    @Test
    fun `should return null when enrollment API fails`() {
        every { edLinkEnrollmentApi.listTeacherEnrollments(token, personId) } returns AccessDeniedError("API_ERROR").left()

        val result = service.listTeacherSubjects(token, personId)

        assertNull(result.getOrNull())
        verify(exactly = 1) { edLinkEnrollmentApi.listTeacherEnrollments(token, personId) }
        verify(exactly = 0) { edLinkClassApi.listClasses(any(), any()) }
        verify(exactly = 0) { edLinkSubjectApi.listSubjects(any(), any()) }
    }

    @Test
    fun `should return null when class API fails`() {
        val classId = UUID.randomUUID()
        val enrollments = listOf(createEnrollment(classId = classId))

        every { edLinkEnrollmentApi.listTeacherEnrollments(token, personId) } returns enrollments.right()
        every { edLinkClassApi.listClasses(token, listOf(classId)) } returns AccessDeniedError("API_ERROR").left()

        val result = service.listTeacherSubjects(token, personId)

        assertNull(result.getOrNull())
        verify(exactly = 1) { edLinkEnrollmentApi.listTeacherEnrollments(token, personId) }
        verify(exactly = 1) { edLinkClassApi.listClasses(token, listOf(classId)) }
        verify(exactly = 0) { edLinkSubjectApi.listSubjects(any(), any()) }
    }

    @Test
    fun `should return null when subject API fails`() {
        val classId = UUID.randomUUID()
        val subjectId = UUID.randomUUID()

        val enrollments = listOf(createEnrollment(classId = classId))
        val classes = listOf(createClass(id = classId, subjectId = subjectId))

        every { edLinkEnrollmentApi.listTeacherEnrollments(token, personId) } returns enrollments.right()
        every { edLinkClassApi.listClasses(token, listOf(classId)) } returns classes.right()
        every { edLinkSubjectApi.listSubjects(token, listOf(subjectId)) } returns AccessDeniedError("API_ERROR").left()

        val result = service.listTeacherSubjects(token, personId)

        assertNull(result.getOrNull())
        verify(exactly = 1) { edLinkEnrollmentApi.listTeacherEnrollments(token, personId) }
        verify(exactly = 1) { edLinkClassApi.listClasses(token, listOf(classId)) }
        verify(exactly = 1) { edLinkSubjectApi.listSubjects(token, listOf(subjectId)) }
    }

    private fun createEnrollment(
        id: UUID = UUID.randomUUID(),
        personId: UUID = this.personId,
        classId: UUID,
        role: String = "teacher",
        state: String = "active"
    ): Enrollment {
        return Enrollment(id, personId, classId, role, state)
    }

    private fun createClass(
        id: UUID,
        schoolId: UUID = UUID.randomUUID(),
        name: String = "Test Class",
        subjectId: UUID?
    ): EdLinkClass {
        return EdLinkClass(id, schoolId, name, subjectId)
    }

    private fun createSubject(
        id: UUID,
        name: String
    ): Subject {
        return Subject(id, name)
    }
}
