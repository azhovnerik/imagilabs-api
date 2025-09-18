package com.anahoret.imagilabsapi.lovable.domain

import arrow.core.Either
import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomAccessService
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.testTeacher
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
        val classroom = Classroom(classroomId, "c", "ac", 0, 0, teacher.id, 1)
        every { classroomService.getById(classroomId) } returns classroom
        every { classroomAccessService.canUpdateClassroom(teacher, classroom) } returns false

        val result = useCase.setPaused(teacher, classroomId, true)

        assertTrue(result is Either.Left)
        assertTrue((result as Either.Left).value is com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError)
        verify(exactly = 0) { lovableClassroomService.setPausedIntegrationForClassroom(any(), any()) }
    }

    @Test
    fun `sets paused when allowed`() {
        val teacher = testTeacher()
        val classroomId = UUID.randomUUID()
        val classroom = Classroom(classroomId, "c", "ac", 0, 0, teacher.id, 1)
        every { classroomService.getById(classroomId) } returns classroom
        every { classroomAccessService.canUpdateClassroom(teacher, classroom) } returns true

        val result = useCase.setPaused(teacher, classroomId, true)

        assertTrue(result is Either.Right)
        verify { lovableClassroomService.setPausedIntegrationForClassroom(classroomId, true) }
    }
}
