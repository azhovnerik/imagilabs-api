package com.anahoret.imagilabsapi.lovable.domain

import arrow.core.Either
import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomAccessService
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.testTeacher
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class GetLovableCredentialsForClassroomUseCaseImplTest {

    private lateinit var classroomAccessService: ClassroomAccessService
    private lateinit var classroomService: ClassroomService
    private lateinit var lovableAccountService: LovableAccountService
    private lateinit var studentProfileService: StudentProfileService
    private lateinit var useCase: GetLovableCredentialsForClassroomUseCase

    @BeforeEach
    fun setUp() {
        classroomAccessService = mockk()
        classroomService = mockk()
        lovableAccountService = mockk()
        studentProfileService = mockk()
        useCase = GetLovableCredentialsForClassroomUseCaseImpl(
            classroomAccessService,
            classroomService,
            lovableAccountService,
            studentProfileService
        )
    }

    @Test
    fun `returns NotFound when classroom missing`() {
        val teacher = testTeacher()
        val classroomId = UUID.randomUUID()
        every { classroomService.getById(classroomId) } returns null

        val result = useCase.getCredentials(teacher, classroomId)

        assertTrue(result is Either.Left)
        assertTrue((result as Either.Left).value is com.anahoret.imagilabsapi.common.domain.error.NotFoundError)
    }

    @Test
    fun `returns AccessDenied when teacher cannot list credentials`() {
        val teacher = testTeacher()
        val classroomId = UUID.randomUUID()
        val classroom = Classroom(classroomId, "c", "ac", 0, 0, teacher.id, 1, blocked = false)
        every { classroomService.getById(classroomId) } returns classroom
        every { classroomAccessService.canListStudentCredentials(teacher, classroom) } returns false

        val result = useCase.getCredentials(teacher, classroomId)

        assertTrue(result is Either.Left)
        assertTrue((result as Either.Left).value is com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError)
    }

    @Test
    fun `returns credentials for all students in classroom`() {
        val teacher = testTeacher()
        val classroomId = UUID.randomUUID()
        val classroom = Classroom(classroomId, "c", "ac", 0, 0, teacher.id, 1, blocked = false)
        val s1 = StudentProfile(UUID.randomUUID(), "s1", "u1", 1L, classroomId)
        val s2 = StudentProfile(UUID.randomUUID(), "s2", "u2", 1L, classroomId)
        val a1 = LovableAccount(s1.id, "u1", "e1@x.com", "p1")
        val a2 = LovableAccount(s2.id, "u2", "e2@x.com", "p2")
        every { classroomService.getById(classroomId) } returns classroom
        every { classroomAccessService.canListStudentCredentials(teacher, classroom) } returns true
        every { studentProfileService.listByClassroom(classroomId) } returns listOf(s1, s2)
        every {
            lovableAccountService.getByConnectedUsers(match {
                it.containsAll(
                    listOf(
                        s1.id,
                        s2.id
                    )
                )
            })
        } returns listOf(a1, a2)

        val result = useCase.getCredentials(teacher, classroomId)

        assertTrue(result is Either.Right)
        val accounts = (result as Either.Right).value
        assertEquals(2, accounts.size)
        assertTrue(accounts.containsAll(listOf(a1, a2)))
        verify { lovableAccountService.getByConnectedUsers(match { it.containsAll(listOf(s1.id, s2.id)) }) }
    }
}
