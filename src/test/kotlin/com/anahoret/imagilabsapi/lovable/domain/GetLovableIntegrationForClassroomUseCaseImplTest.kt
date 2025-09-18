package com.anahoret.imagilabsapi.lovable.domain

import arrow.core.Either
import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomAccessService
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.common.testStudent
import com.anahoret.imagilabsapi.common.testTeacher
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

class GetLovableIntegrationForClassroomUseCaseImplTest {

    private val lovableClassroomService: LovableClassroomService = mockk()
    private val classroomAccessService: ClassroomAccessService = mockk()
    private val classroomService: ClassroomService = mockk()
    private val useCase: GetLovableIntegrationForClassroomUseCase = GetLovableIntegrationForClassroomUseCaseImpl(
        lovableClassroomService,
        classroomAccessService,
        classroomService
    )

    @Test
    fun `returns NotFound when classroom does not exist`() {
        val teacher = testTeacher()
        val classroomId = UUID.randomUUID()
        every { classroomService.getById(classroomId) } returns null

        val result = useCase.get(teacher, classroomId)

        assertTrue(result is Either.Left)
        val err = (result as Either.Left).value
        assertTrue(err is NotFoundError)
        verify { classroomService.getById(classroomId) }
    }

    @Test
    fun `returns AccessDenied when user cannot access classroom`() {
        val student = testStudent()
        val classroomId = UUID.randomUUID()
        val classroom = Classroom(classroomId, "A", "X", 0, 0, student.id, 1)
        every { classroomService.getById(classroomId) } returns classroom
        every { classroomAccessService.canGetClassroom(student, classroom) } returns false

        val result = useCase.get(student, classroomId)

        assertTrue(result is Either.Left)
        val err = (result as Either.Left).value
        assertTrue(err is AccessDeniedError)
        verify { classroomService.getById(classroomId) }
        verify { classroomAccessService.canGetClassroom(student, classroom) }
    }

    @Test
    fun `returns Right when integration exists and access granted`() {
        val teacher = testTeacher()
        val classroomId = UUID.randomUUID()
        val classroom = Classroom(classroomId, "B", "Y", 0, 0, teacher.id, 1)
        val integration = LovableClassroom(classroomId, true, false)
        every { classroomService.getById(classroomId) } returns classroom
        every { classroomAccessService.canGetClassroom(teacher, classroom) } returns true
        every { lovableClassroomService.getIntegrationForClassroom(classroomId) } returns integration

        val result = useCase.get(teacher, classroomId)

        assertTrue(result is Either.Right)
        assertEquals(integration, (result as Either.Right).value)
        verify { classroomService.getById(classroomId) }
        verify { classroomAccessService.canGetClassroom(teacher, classroom) }
        verify { lovableClassroomService.getIntegrationForClassroom(classroomId) }
    }

    @Test
    fun `returns NotFound when integration missing`() {
        val teacher = testTeacher()
        val classroomId = UUID.randomUUID()
        val classroom = Classroom(classroomId, "C", "Z", 0, 0, teacher.id, 1)
        every { classroomService.getById(classroomId) } returns classroom
        every { classroomAccessService.canGetClassroom(teacher, classroom) } returns true
        every { lovableClassroomService.getIntegrationForClassroom(classroomId) } returns null

        val result = useCase.get(teacher, classroomId)

        assertTrue(result is Either.Left)
        val err = (result as Either.Left).value
        assertTrue(err is NotFoundError)
        verify { classroomService.getById(classroomId) }
        verify { classroomAccessService.canGetClassroom(teacher, classroom) }
        verify { lovableClassroomService.getIntegrationForClassroom(classroomId) }
    }
}
