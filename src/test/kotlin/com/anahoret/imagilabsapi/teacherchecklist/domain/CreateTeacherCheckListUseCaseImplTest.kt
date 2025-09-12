package com.anahoret.imagilabsapi.teacherchecklist.domain

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.testTeacherCheckList
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("Create teacher check list use case")
class CreateTeacherCheckListUseCaseImplTest {

    private val teacherProfileService = mockk<TeacherProfileService>()
    private val teacherCheckListService = mockk<TeacherCheckListService>()
    private val createTeacherCheckListUseCase = CreateTeacherCheckListUseCaseImpl(teacherProfileService,teacherCheckListService)

    private val teacherId = UUID.randomUUID()

    @Test
    fun `should return not found error when teacher not found`() {
        every { teacherProfileService.exists(teacherId) } returns false
        when(val result = createTeacherCheckListUseCase.create(teacherId)){
            is Either.Left -> assertTrue(result.value is NotFoundError)
            is Either.Right -> fail()
        }
    }

    @Test
    fun `should create teacher check list`() {
        val teacherCheckList = testTeacherCheckList()
        every { teacherProfileService.exists(teacherId) } returns true
        every { teacherCheckListService.createCheckList(teacherId) } returns teacherCheckList
        when(val result = createTeacherCheckListUseCase.create(teacherId)){
            is Either.Left -> fail()
            is Either.Right -> assertAll(
                { assertEquals(teacherCheckList.checkListSteps[0].step, result.value.checkListSteps[0].step, "Incorrect step.") },
                { assertEquals(teacherCheckList.checkListSteps[0].completed, result.value.checkListSteps[0].completed, "Incorrect completed.") }
            )
        }
    }
}
