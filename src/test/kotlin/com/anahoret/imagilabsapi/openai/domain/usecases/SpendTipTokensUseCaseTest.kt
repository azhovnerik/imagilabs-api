package com.anahoret.imagilabsapi.openai.domain.usecases

import arrow.core.right
import com.anahoret.imagilabsapi.admins.domain.AdminProfile
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.openai.domain.TipTokensResponse
import com.anahoret.imagilabsapi.openai.domain.TipTokensService
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import com.anahoret.imagilabsapi.users.UserType
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("Spend tip tokens use case")
class SpendTipTokensUseCaseTest {

    private val tipTokensService = mockk<TipTokensService>()
    private val studentProfileService = mockk<StudentProfileService>()
    private val teacherProfileService = mockk<TeacherProfileService>()
    private val getTipTokensUseCase = mockk<GetTipTokensUseCase>()
    private val spendTipTokensUseCase =
        SpendTipTokensUseCaseImpl(studentProfileService, teacherProfileService, tipTokensService, getTipTokensUseCase)

    private val studentId = UUID.randomUUID()
    private val teacherId = UUID.randomUUID()
    private val studentProfile = mockk<StudentProfile> {
        every { id } returns studentId
        every { userType } returns UserType.STUDENT
    }

    private val teacherProfile = mockk<TeacherProfile> {
        every { id } returns teacherId
        every { userType } returns UserType.STUDENT
    }

    private val adminProfile = mockk<AdminProfile> {
        every { id } returns teacherId
        every { userType } returns UserType.STUDENT
    }

    @Test
    fun `should return error when student not found`() {
        every { studentProfileService.getStudentById(studentId) } returns null
        spendTipTokensUseCase.spend(studentProfile).fold(
            {
                assertAll(
                    { assertTrue(it is NotFoundError) },
                    { assertEquals("STUDENT_NOT_FOUND", (it as NotFoundError).message) }
                )
            }, { fail() }
        )
    }

    @Test
    fun `should return error when teacher not found`() {
        every { teacherProfileService.getTeacherById(teacherId) } returns null
        spendTipTokensUseCase.spend(teacherProfile).fold(
            {
                assertAll(
                    { assertTrue(it is NotFoundError) },
                    { assertEquals("TEACHER_NOT_FOUND", (it as NotFoundError).message) }
                )
            }, { fail() }
        )
    }

    @Test
    fun `should return error when user is not teacher nor student`() {
        val userId = UUID.randomUUID()
        every { studentProfileService.getStudentById(userId) } returns null
        every { teacherProfileService.getTeacherById(userId) } returns null
        spendTipTokensUseCase.spend(adminProfile).fold(
            {
                assertAll(
                    { assertTrue(it is NotFoundError) },
                    { assertEquals("USER_NOT_FOUND", (it as NotFoundError).message) }
                )
            }, { fail() }
        )
    }

    @Test
    fun `should spend tip token data for student`() {
        every { studentProfileService.getStudentById(studentId) } returns mockk()
        justRun { tipTokensService.withdrawOneTipToken(studentProfile) }
        every { getTipTokensUseCase.get(studentProfile) } returns TipTokensResponse(3, 60L).right()
        spendTipTokensUseCase.spend(studentProfile).fold({ fail() }, { assertEquals(3, it.leftTipTokens) })
    }

    @Test
    fun `should spend tip token data for teacher`() {
        every { teacherProfileService.getTeacherById(teacherId) } returns mockk()
        justRun { tipTokensService.withdrawOneTipToken(teacherProfile) }
        every { getTipTokensUseCase.get(teacherProfile) } returns TipTokensResponse(3, 60L).right()
        spendTipTokensUseCase.spend(teacherProfile).fold({ fail() }, { assertEquals(3, it.leftTipTokens) })
    }

}
