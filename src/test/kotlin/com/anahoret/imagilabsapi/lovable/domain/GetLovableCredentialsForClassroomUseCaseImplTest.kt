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
import com.anahoret.imagilabsapi.userclassroomlink.domain.StudentClassroomLinkService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

class GetLovableCredentialsForClassroomUseCaseImplTest {

    private val classroomAccessService = mockk<ClassroomAccessService>()
    private val classroomService = mockk<ClassroomService>()
    private val lovableAccountService = mockk<LovableAccountService>()
    private val studentClassroomLinkService = mockk<StudentClassroomLinkService>()
    private val getLovableCredentialsForClassroomUseCase = GetLovableCredentialsForClassroomUseCaseImpl(
        classroomAccessService,
        classroomService,
        lovableAccountService,
        studentClassroomLinkService
    )

    @Test
    fun `returns NotFound when classroom missing`() {
        val teacher = testTeacher()
        val classroomId = UUID.randomUUID()
        every { classroomService.getById(classroomId) } returns null

        val result = getLovableCredentialsForClassroomUseCase.getCredentials(teacher, classroomId)

        assertTrue(result is Either.Left)
        assertTrue((result as Either.Left).value is com.anahoret.imagilabsapi.common.domain.error.NotFoundError)
    }

    @Test
    fun `returns AccessDenied when teacher cannot list credentials`() {
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
            ClassroomPermissions(true),
            deleted = false
        )
        every { classroomService.getById(classroomId) } returns classroom
        every { classroomAccessService.canListStudentCredentials(teacher, classroom) } returns false

        val result = getLovableCredentialsForClassroomUseCase.getCredentials(teacher, classroomId)

        assertTrue(result is Either.Left)
        assertTrue((result as Either.Left).value is AccessDeniedError)
    }

    @Test
    fun `returns AccessDenied when classroom is blocked`() {
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
            ClassroomPermissions(true),
            deleted = false
        )
        every { classroomService.getById(classroomId) } returns classroom

        val result = getLovableCredentialsForClassroomUseCase.getCredentials(teacher, classroomId)

        assertEquals(AccessDeniedError("SUBSCRIPTION_REQUIRED").left(), result)
    }

    @Test
    fun `returns credentials for all students in classroom`() {
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
            ClassroomPermissions(true),
            deleted = false
        )
        val s1 = StudentProfile(UUID.randomUUID(), "s1", "u1", 1L)
        val s2 = StudentProfile(UUID.randomUUID(), "s2", "u2", 1L)
        val a1 = LovableAccount(s1.id, "s1", "u1", "e1@x.com", "p1")
        val a2 = LovableAccount(s2.id, "s2", "u2", "e2@x.com", "p2")
        every { classroomService.getById(classroomId) } returns classroom
        every { classroomAccessService.canListStudentCredentials(teacher, classroom) } returns true
        every { studentClassroomLinkService.listStudentsByClassroom(classroomId) } returns listOf(s1, s2)
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

        val result = getLovableCredentialsForClassroomUseCase.getCredentials(teacher, classroomId)

        assertTrue(result is Either.Right)
        val accounts = (result as Either.Right).value
        assertEquals(2, accounts.size)
        assertTrue(accounts.containsAll(listOf(a1, a2)))
        verify { lovableAccountService.getByConnectedUsers(match { it.containsAll(listOf(s1.id, s2.id)) }) }
    }
}
