package com.anahoret.imagilabsapi.openai.domain.usecases

import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID

@DisplayName("Get tip tokens use case")
class GetTipTokensUseCaseImplTest {

    private val studentProfileService = mockk<StudentProfileService>()
    private val getTipTokensUseCase = GetTipTokensUseCaseImpl(studentProfileService)

    private val studentId = UUID.randomUUID()
    private val userProfile = mockk<UserProfile> {
        every { id } returns studentId
    }

    @Test
    fun `should return error when student not found`() {
        every { studentProfileService.getStudentById(studentId) } returns null
        getTipTokensUseCase.get(userProfile).fold(
            {
                assertAll(
                    { assertTrue(it is NotFoundError) },
                    { assertEquals("STUDENT_NOT_FOUND", (it as NotFoundError).message) }
                )
            }, { fail() }
        )
    }

    @Test
    fun `should return tip tokens data`() {
        every { studentProfileService.getStudentById(studentId) } returns mockk {
            every { tipTokens } returns 3
        }
        getTipTokensUseCase.get(userProfile).fold({ fail() }, { assertEquals(3, it.leftTipTokens) })
    }
}
