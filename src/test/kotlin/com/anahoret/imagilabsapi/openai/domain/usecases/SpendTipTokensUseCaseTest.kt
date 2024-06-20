package com.anahoret.imagilabsapi.openai.domain.usecases

import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.openai.domain.TipTokensResponse
import com.anahoret.imagilabsapi.openai.domain.TipTokensService
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("Spend tip tokens usecase")
class SpendTipTokensUseCaseTest {

    private val tipTokensService = mockk<TipTokensService>()
    private val studentProfileService = mockk<StudentProfileService>()
    private val getTipTokensUseCase = mockk<GetTipTokensUseCase>()
    private val spendTipTokensUseCase =
        SpendTipTokensUseCaseImpl(studentProfileService, tipTokensService, getTipTokensUseCase)

    private val studentId = UUID.randomUUID()
    private val userProfile = mockk<UserProfile> {
        every { id } returns studentId
    }

    @Test
    fun `should return error when student not found`() {
        every { studentProfileService.getStudentById(studentId) } returns null
        spendTipTokensUseCase.spend(userProfile).fold(
            {
                assertAll(
                    { assertTrue(it is NotFoundError) },
                    { assertEquals("STUDENT_NOT_FOUND", (it as NotFoundError).message) }
                )
            }, { fail() }
        )
    }

    @Test
    fun `should spend tip token data`() {
        every { studentProfileService.getStudentById(studentId) } returns mockk()
        justRun { tipTokensService.withdrawOneTipToken(studentId) }
        every { getTipTokensUseCase.get(userProfile) } returns TipTokensResponse(3, 60L).right()
        spendTipTokensUseCase.spend(userProfile).fold({ fail() }, { assertEquals(3, it.leftTipTokens) })
    }

}
