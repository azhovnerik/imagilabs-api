package com.anahoret.imagilabsapi.lovable.domain

import arrow.core.Either
import com.anahoret.imagilabsapi.common.testTeacher
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ConnectLovableAccountToTeacherUseCaseTest {

    private val service: LovableAccountService = mockk()
    private val useCase: ConnectLovableAccountToTeacherUseCase = ConnectLovableAccountToTeacherUseCaseImpl(service)

    @Test
    fun `returns Left when max number of connected accounts reached`() {
        val teacher = testTeacher()
        every { service.connectedCount(teacher.id) } returns Settings.MAX_CONNECTED_ACCOUNTS.toLong()

        val result = useCase.connect(teacher)

        assertTrue(result is Either.Left)
        assertTrue((result as Either.Left).value is MaxNumberOfConnectedAccountsExceededError)
    }

    @Test
    fun `returns Left when no free lovable accounts`() {
        val teacher = testTeacher()
        every { service.connectedCount(teacher.id) } returns (Settings.MAX_CONNECTED_ACCOUNTS - 1).toLong()
        every { service.connectToUser(teacher) } returns null

        val result = useCase.connect(teacher)

        assertTrue(result is Either.Left)
        assertTrue((result as Either.Left).value is OutOfLovableAccountsError)
    }

    @Test
    fun `returns Right with connected account`() {
        val teacher = testTeacher()
        val account = LovableAccount("a@x.com", "p")
        every { service.connectedCount(teacher.id) } returns 0
        every { service.connectToUser(teacher) } returns account

        val result = useCase.connect(teacher)

        assertTrue(result is Either.Right)
        assertEquals(account, (result as Either.Right).value)
    }
}
