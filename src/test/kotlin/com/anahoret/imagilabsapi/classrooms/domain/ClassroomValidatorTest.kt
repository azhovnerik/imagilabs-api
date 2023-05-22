package com.anahoret.imagilabsapi.classrooms.domain

import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.userclassroomlink.domain.StudentClassroomLinkService
import io.mockk.every
import io.mockk.mockk
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("Classroom validator")
class ClassroomValidatorTest {

    private val studentClassroomLinkService = mockk<StudentClassroomLinkService>()
    private val teacherSubscriptionService = mockk<TeacherSubscriptionService>()

    private val classroomValidator = ClassroomValidatorImpl(
        studentClassroomLinkService,
        teacherSubscriptionService
    )

    @DisplayName("when validating classroom create request")
    @Nested
    inner class ValidateClassroomCreateRequest {

        private val teacherProfile = mockk<TeacherProfile>()

        @Test
        fun `should return error if classroom name is empty`() {
            val classroomCreateRequest = mockk<ClassroomCreateRequest> {
                every { name } returns ""
                every { studentCreateRequests } returns emptyList()
            }
            every { teacherSubscriptionService.studentLimitPerClassExceeded(teacherProfile, 0) } returns false
            val result = classroomValidator.validate(teacherProfile, classroomCreateRequest)
            assertEquals(listOf(ValidationError.FieldIsBlank("NAME")).left(), result)
        }

        @Test
        fun `should return error if classroom name is blank`() {
            val classroomCreateRequest = mockk<ClassroomCreateRequest> {
                every { name } returns "       "
                every { studentCreateRequests } returns emptyList()
            }
            every { teacherSubscriptionService.studentLimitPerClassExceeded(teacherProfile, 0) } returns false
            val result = classroomValidator.validate(teacherProfile, classroomCreateRequest)
            assertEquals(listOf(ValidationError.FieldIsBlank("NAME")).left(), result)
        }

        @Test
        fun `should return error if classroom name is longer than 50 characters`() {
            val classroomCreateRequest = mockk<ClassroomCreateRequest> {
                every { name } returns RandomStringUtils.randomAlphabetic(51)
                every { studentCreateRequests } returns emptyList()
            }
            every { teacherSubscriptionService.studentLimitPerClassExceeded(teacherProfile, 0) } returns false
            val result = classroomValidator.validate(teacherProfile, classroomCreateRequest)
            assertEquals(listOf(ValidationError.FieldIsTooLong("NAME")).left(), result)
        }

        @Test
        fun `should return error if classroom has too many students`() {
            val classroomCreateRequest = mockk<ClassroomCreateRequest> {
                every { name } returns "Name"
                every { studentCreateRequests } returns emptyList()
            }
            every { teacherSubscriptionService.studentLimitPerClassExceeded(teacherProfile, 0) } returns true
            val result = classroomValidator.validate(teacherProfile, classroomCreateRequest)
            assertEquals(listOf(ValidationError("TOO_MANY_STUDENTS")).left(), result)
        }

        @Test
        fun `should return all errors`() {
            val classroomCreateRequest = mockk<ClassroomCreateRequest> {
                every { name } returns ""
                every { studentCreateRequests } returns emptyList()
            }
            every { teacherSubscriptionService.studentLimitPerClassExceeded(teacherProfile, 0) } returns true
            val result = classroomValidator.validate(teacherProfile, classroomCreateRequest)
            assertEquals(listOf(ValidationError.FieldIsBlank("NAME"), ValidationError("TOO_MANY_STUDENTS")).left(), result)
        }

        @Test
        fun `should return no errors if classroom create request is valid`() {
            val classroomCreateRequest = mockk<ClassroomCreateRequest> {
                every { name } returns "Name"
                every { studentCreateRequests } returns emptyList()
            }
            every { teacherSubscriptionService.studentLimitPerClassExceeded(teacherProfile, 0) } returns false
            val result = classroomValidator.validate(teacherProfile, classroomCreateRequest)
            assertEquals(Unit.right(), result)
        }

    }


    @DisplayName("when validating classroom update request")
    @Nested
    inner class ValidateClassroomUpdateRequest {

        private val teacherProfile = mockk<TeacherProfile>()
        private val classroomId = UUID.randomUUID()

        @Test
        fun `should return error if classroom name is empty`() {
            val classroomCreateRequest = mockk<ClassroomUpdateRequest> {
                every { name } returns ""
                every { studentCreateRequests } returns emptyList()
            }
            every { studentClassroomLinkService.getStudentCount(classroomId) } returns 10
            every { teacherSubscriptionService.studentLimitPerClassExceeded(teacherProfile, 10) } returns false
            val result = classroomValidator.validate(teacherProfile, classroomId, classroomCreateRequest)
            assertEquals(listOf(ValidationError.FieldIsBlank("NAME")).left(), result)
        }

        @Test
        fun `should return error if classroom name is blank`() {
            val classroomCreateRequest = mockk<ClassroomUpdateRequest> {
                every { name } returns "       "
                every { studentCreateRequests } returns emptyList()
            }
            every { studentClassroomLinkService.getStudentCount(classroomId) } returns 10
            every { teacherSubscriptionService.studentLimitPerClassExceeded(teacherProfile, 10) } returns false
            val result = classroomValidator.validate(teacherProfile, classroomId, classroomCreateRequest)
            assertEquals(listOf(ValidationError.FieldIsBlank("NAME")).left(), result)
        }

        @Test
        fun `should return error if classroom name is longer than 50 characters`() {
            val classroomCreateRequest = mockk<ClassroomUpdateRequest> {
                every { name } returns RandomStringUtils.randomAlphabetic(51)
                every { studentCreateRequests } returns emptyList()
            }
            every { studentClassroomLinkService.getStudentCount(classroomId) } returns 10
            every { teacherSubscriptionService.studentLimitPerClassExceeded(teacherProfile, 10) } returns false
            val result = classroomValidator.validate(teacherProfile, classroomId, classroomCreateRequest)
            assertEquals(listOf(ValidationError.FieldIsTooLong("NAME")).left(), result)
        }

        @Test
        fun `should return error if classroom has too many students`() {
            val classroomCreateRequest = mockk<ClassroomUpdateRequest> {
                every { name } returns "Name"
                every { studentCreateRequests } returns emptyList()
            }
            every { studentClassroomLinkService.getStudentCount(classroomId) } returns 10
            every { teacherSubscriptionService.studentLimitPerClassExceeded(teacherProfile, 10) } returns true
            val result = classroomValidator.validate(teacherProfile, classroomId, classroomCreateRequest)
            assertEquals(listOf(ValidationError("TOO_MANY_STUDENTS")).left(), result)
        }

        @Test
        fun `should return all errors`() {
            val classroomCreateRequest = mockk<ClassroomUpdateRequest> {
                every { name } returns ""
                every { studentCreateRequests } returns emptyList()
            }
            every { studentClassroomLinkService.getStudentCount(classroomId) } returns 10
            every { teacherSubscriptionService.studentLimitPerClassExceeded(teacherProfile, 10) } returns true
            val result = classroomValidator.validate(teacherProfile, classroomId, classroomCreateRequest)
            assertEquals(listOf(ValidationError.FieldIsBlank("NAME"), ValidationError("TOO_MANY_STUDENTS")).left(), result)
        }

        @Test
        fun `should return no errors if classroom create request is valid`() {
            val classroomCreateRequest = mockk<ClassroomUpdateRequest> {
                every { name } returns "Name"
                every { studentCreateRequests } returns emptyList()
            }
            every { studentClassroomLinkService.getStudentCount(classroomId) } returns 10
            every { teacherSubscriptionService.studentLimitPerClassExceeded(teacherProfile, 10) } returns false
            val result = classroomValidator.validate(teacherProfile, classroomId, classroomCreateRequest)
            assertEquals(Unit.right(), result)
        }

    }
}
