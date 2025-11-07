package com.anahoret.imagilabsapi.lovable.domain

import arrow.core.Either
import arrow.core.left
import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomAccessService
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomPermissions
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.common.testTeacher
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class EnableLovableIntegrationForClassroomUseCaseImplTest {

    private lateinit var lovableClassroomService: LovableClassroomService
    private lateinit var studentProfileService: StudentProfileService
    private lateinit var lovableAccountService: LovableAccountService
    private lateinit var connectUseCase: ConnectLovableAccountToUserUseCase
    private lateinit var classroomAccessService: ClassroomAccessService
    private lateinit var classroomService: ClassroomService
    private lateinit var useCase: EnableLovableIntegrationForClassroomUseCase
    private lateinit var getLovableIntegrationForClassroomUseCase: GetLovableIntegrationForClassroomUseCase

    @BeforeEach
    fun setUp() {
        lovableClassroomService = mockk(relaxed = true)
        studentProfileService = mockk()
        lovableAccountService = mockk()
        connectUseCase = mockk(relaxed = true)
        classroomAccessService = mockk()
        classroomService = mockk()
        getLovableIntegrationForClassroomUseCase = mockk()
        useCase = EnableLovableIntegrationForClassroomUseCaseImpl(
            lovableClassroomService,
            studentProfileService,
            lovableAccountService,
            connectUseCase,
            classroomAccessService,
            classroomService,
            getLovableIntegrationForClassroomUseCase
        )
    }

    @Test
    fun `returns NotFound when classroom missing`() {
        val teacher = testTeacher()
        val classroomId = UUID.randomUUID()
        every { classroomService.getById(classroomId) } returns null

        val result = useCase.enable(teacher, classroomId)

        assertTrue(result is Either.Left)
        assertTrue((result as Either.Left).value is com.anahoret.imagilabsapi.common.domain.error.NotFoundError)
        verify { classroomService.getById(classroomId) }
        verify(exactly = 0) { lovableClassroomService.enableIntegrationForClassroom(any()) }
    }

    @Test
    fun `returns AccessDenied when teacher cannot update classroom`() {
        val teacher = testTeacher()
        val classroomId = UUID.randomUUID()
        val classroom = Classroom(
            classroomId,
            "c",
            "ac",
            0,
            0,
            teacher.id,
            1,
            blocked = false,
            ClassroomPermissions(true)
        )
        every { classroomService.getById(classroomId) } returns classroom
        every { classroomAccessService.canUpdateClassroom(teacher, classroom) } returns false

        val result = useCase.enable(teacher, classroomId)

        assertTrue(result is Either.Left)
        assertTrue((result as Either.Left).value is AccessDeniedError)
        verify { classroomService.getById(classroomId) }
        verify { classroomAccessService.canUpdateClassroom(teacher, classroom) }
        verify(exactly = 0) { lovableClassroomService.enableIntegrationForClassroom(any()) }
    }

    @Test
    fun `enables integration and connects students without active accounts`() {
        val teacher = testTeacher()
        val classroomId = UUID.randomUUID()
        val classroom = Classroom(
            classroomId,
            "c",
            "ac",
            0,
            0,
            teacher.id,
            1,
            blocked = false,
            ClassroomPermissions(true)
        )
        val s1 = StudentProfile(UUID.randomUUID(), "s1", "u1", 1L, classroomId)
        val s2 = StudentProfile(UUID.randomUUID(), "s2", "u2", 1L, classroomId)
        val mockLovableClassroom = mockk<LovableClassroom>()
        every { classroomService.getById(classroomId) } returns classroom
        every { classroomAccessService.canUpdateClassroom(teacher, classroom) } returns true
        every { studentProfileService.listByClassroom(classroomId) } returns listOf(s1, s2)
        every { lovableAccountService.getActive(s1) } returns LovableAccount(s1.id, "s1", "u1", "e", "p")
        every { lovableAccountService.getActive(s2) } returns null
        every { getLovableIntegrationForClassroomUseCase.get(teacher, classroomId) } returns Either.Right(
            mockLovableClassroom
        )

        val result = useCase.enable(teacher, classroomId)

        assertTrue(result is Either.Right)
        assertEquals(mockLovableClassroom, (result as Either.Right).value)
        verifySequence {
            classroomService.getById(classroomId)
            classroomAccessService.canUpdateClassroom(teacher, classroom)
            lovableClassroomService.enableIntegrationForClassroom(classroomId)
            studentProfileService.listByClassroom(classroomId)
            lovableAccountService.getActive(s1)
            lovableAccountService.getActive(s2)
            connectUseCase.connect(s2)
        }
        verify(inverse = true) { connectUseCase.connect(s1) }
    }

    @Test
    fun `returns access denied error when classroom is blocked`() {
        val teacher = testTeacher()
        val classroomId = UUID.randomUUID()
        val classroom = Classroom(
            classroomId,
            "c",
            "ac",
            0,
            0,
            teacher.id,
            1,
            blocked = true,
            ClassroomPermissions(true)
        )
        every { classroomService.getById(classroomId) } returns classroom

        val result = useCase.enable(teacher, classroomId)

        assertEquals(AccessDeniedError("SUBSCRIPTION_REQUIRED").left(), result)
    }
}
