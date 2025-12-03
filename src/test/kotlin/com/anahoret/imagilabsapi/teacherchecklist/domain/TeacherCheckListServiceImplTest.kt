package com.anahoret.imagilabsapi.teacherchecklist.domain

import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStep.CONGRATULATION_DIALOG_SHOWN
import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStepEntity
import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStepRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("Teacher checklist service")
class TeacherCheckListServiceImplTest {

    private val teacherCheckListRepository = mockk<TeacherCheckListStepRepository>()
    private val service = TeacherCheckListServiceImpl(teacherCheckListRepository)
    private val teacherId = UUID.randomUUID()

    @Test
    fun `should not reset congratulation dialog when it is already completed`() {
        val completedCongratulationStep = TeacherCheckListStepEntity(
            teacherId = teacherId,
            step = CONGRATULATION_DIALOG_SHOWN,
            completed = true
        )

        every { teacherCheckListRepository.findAllByTeacherId(teacherId) } returns listOf(completedCongratulationStep)

        service.resetCongratulationDialog(teacherId)

        verify(exactly = 0) { teacherCheckListRepository.save(any()) }
    }

    @Test
    fun `should create congratulation dialog step when it does not exist`() {
        every { teacherCheckListRepository.findAllByTeacherId(teacherId) } returns emptyList()
        every { teacherCheckListRepository.save(any()) } returns TeacherCheckListStepEntity(
            teacherId = teacherId,
            step = CONGRATULATION_DIALOG_SHOWN,
            completed = false
        )

        service.resetCongratulationDialog(teacherId)

        verify {
            teacherCheckListRepository.save(
                withArg {
                    assertFalse(it.completed)
                    assertEquals(CONGRATULATION_DIALOG_SHOWN, it.step)
                }
            )
        }
    }
}
