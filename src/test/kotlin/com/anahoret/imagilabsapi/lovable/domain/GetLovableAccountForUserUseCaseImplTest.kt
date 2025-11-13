package com.anahoret.imagilabsapi.lovable.domain

import com.anahoret.imagilabsapi.common.testStudent
import com.anahoret.imagilabsapi.common.testTeacher
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import java.util.*

class GetLovableAccountForUserUseCaseImplTest {

    private val lovableAccountService: LovableAccountService = mockk()
    private val lovableClassroomService = mockk<LovableClassroomService>()
    private val getLovableAccountForUserUseCase: GetLovableAccountForUserUseCase =
        GetLovableAccountForUserUseCaseImpl(lovableAccountService, lovableClassroomService)

    @Test
    fun `get delegates to service and returns account`() {
        val teacher = testTeacher()
        val account = LovableAccount(UUID.randomUUID(), "s", "u", "x@y.com", "pass")
        every { lovableAccountService.getActive(teacher) } returns account

        val result = getLovableAccountForUserUseCase.get(teacher)

        assertSame(account, result)
        verify { lovableAccountService.getActive(teacher) }
    }

    @Test
    fun `get returns null when service returns null`() {
        val teacher = testTeacher()
        every { lovableAccountService.getActive(teacher) } returns null

        val result = getLovableAccountForUserUseCase.get(teacher)

        assertNull(result)
        verify { lovableAccountService.getActive(teacher) }
    }

    @Test
    fun `get returns null for student when integration is not enabled for classroom`() {
        val classroomId = UUID.randomUUID()
        val student = testStudent()

        every { lovableAccountService.getActive(student) } returns mockk()
        every { lovableClassroomService.getIntegrationForClassroom(classroomId) } returns mockk {
            every { lovableIntegrationEnabled } returns false
            every { lovableIntegrationPaused } returns false
        }

        val result = getLovableAccountForUserUseCase.get(student)

        assertNull(result)
        verify(inverse = true) { lovableAccountService.getActive(student) }
        verify { lovableClassroomService.getIntegrationForClassroom(classroomId) }
    }

    @Test
    fun `get returns null for student when integration is paused for classroom`() {
        val classroomId = UUID.randomUUID()
        val student = testStudent()

        every { lovableAccountService.getActive(student) } returns mockk()
        every { lovableClassroomService.getIntegrationForClassroom(classroomId) } returns mockk {
            every { lovableIntegrationEnabled } returns true
            every { lovableIntegrationPaused } returns true
        }

        val result = getLovableAccountForUserUseCase.get(student)

        assertNull(result)
        verify(inverse = true) { lovableAccountService.getActive(student) }
        verify { lovableClassroomService.getIntegrationForClassroom(classroomId) }
    }

    @Test
    fun `get returns profile for student when integration enabled and not paused for classroom`() {
        val classroomId = UUID.randomUUID()
        val student = testStudent()

        every { lovableAccountService.getActive(student) } returns mockk()
        every { lovableClassroomService.getIntegrationForClassroom(classroomId) } returns mockk {
            every { lovableIntegrationEnabled } returns true
            every { lovableIntegrationPaused } returns false
        }

        val result = getLovableAccountForUserUseCase.get(student)

        assertNotNull(result)
        verify { lovableAccountService.getActive(student) }
        verify { lovableClassroomService.getIntegrationForClassroom(classroomId) }
    }
}
