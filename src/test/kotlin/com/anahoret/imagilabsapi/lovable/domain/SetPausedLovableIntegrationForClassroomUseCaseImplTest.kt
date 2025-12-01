package com.anahoret.imagilabsapi.lovable.domain

import arrow.core.Either
import arrow.core.left
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomAccessService
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.common.testClassroom
import com.anahoret.imagilabsapi.common.testTeacher
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class SetPausedLovableIntegrationForClassroomUseCaseImplTest {

    private lateinit var lovableClassroomService: LovableClassroomService
    private lateinit var classroomAccessService: ClassroomAccessService
    private lateinit var classroomService: ClassroomService
    private lateinit var useCase: SetPausedLovableIntegrationForClassroomUseCase

    @BeforeEach
    fun setUp() {
        lovableClassroomService = mockk(relaxed = true)
        classroomAccessService = mockk()
        classroomService = mockk()
        useCase = SetPausedLovableIntegrationForClassroomUseCaseImpl(
            lovableClassroomService,
            classroomAccessService,
            classroomService
        )
    }

    @Test
    fun `returns NotFound when classroom missing`() {
        val teacher = testTeacher()
        val classroomId = UUID.randomUUID()
        every { classroomService.getById(classroomId) } returns null

        val result = useCase.setPaused(teacher, classroomId, true)

        assertTrue(result is Either.Left)
        assertTrue((result as Either.Left).value is com.anahoret.imagilabsapi.common.domain.error.NotFoundError)
        verify(exactly = 0) { lovableClassroomService.setPausedIntegrationForClassroom(any(), any()) }
    }

    @Test
    fun `returns AccessDenied when teacher cannot update classroom`() {
        val teacher = testTeacher()
        val classroomId = UUID.randomUUID()
        val classroom = testClassroom(
            id = classroomId,
            teacherId = teacher.id
        )
        every { classroomService.getById(classroomId) } returns classroom
        every { classroomAccessService.canUpdateClassroom(teacher, classroom) } returns false

        val result = useCase.setPaused(teacher, classroomId, true)

        assertTrue(result is Either.Left)
        assertTrue((result as Either.Left).value is AccessDeniedError)
        verify(exactly = 0) { lovableClassroomService.setPausedIntegrationForClassroom(any(), any()) }
    }

    @Test
    fun `sets paused when allowed`() {
        val teacher = testTeacher()
        val classroomId = UUID.randomUUID()
        val classroom = testClassroom(
            id = classroomId,
            teacherId = teacher.id
        )
        every { classroomService.getById(classroomId) } returns classroom
        every { classroomAccessService.canUpdateClassroom(teacher, classroom) } returns true

        val result = useCase.setPaused(teacher, classroomId, true)

        assertTrue(result is Either.Right)
        verify { lovableClassroomService.setPausedIntegrationForClassroom(classroomId, true) }
    }

    @Test
    fun `returns AccessDenied when classroom is blocked`() {
        val teacher = testTeacher()
        val classroomId = UUID.randomUUID()
        val classroom = testClassroom(
            id = classroomId,
            teacherId = teacher.id,
            blocked = true
        )
        every { classroomService.getById(classroomId) } returns classroom

        val result = useCase.setPaused(teacher, classroomId, true)

        assertEquals(AccessDeniedError("SUBSCRIPTION_REQUIRED").left(), result)
    }
}
