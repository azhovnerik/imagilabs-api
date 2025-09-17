package com.anahoret.imagilabsapi.lovable.domain

import com.anahoret.imagilabsapi.common.testTeacher
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class GetLovableAccountForTeacherUseCaseImplTest {

    private val service: LovableAccountService = mockk()
    private val useCase: GetLovableAccountForTeacherUseCase = GetLovableAccountForTeacherUseCaseImpl(service)

    @Test
    fun `get delegates to service and returns account`() {
        val teacher = testTeacher()
        val account = LovableAccount("x@y.com", "pass")
        every { service.getActive(teacher) } returns account

        val result = useCase.get(teacher)

        assertSame(account, result)
        verify { service.getActive(teacher) }
    }

    @Test
    fun `get returns null when service returns null`() {
        val teacher = testTeacher()
        every { service.getActive(teacher) } returns null

        val result = useCase.get(teacher)

        assertNull(result)
        verify { service.getActive(teacher) }
    }
}
