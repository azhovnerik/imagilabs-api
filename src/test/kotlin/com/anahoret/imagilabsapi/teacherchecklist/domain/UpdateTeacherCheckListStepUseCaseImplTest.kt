package com.anahoret.imagilabsapi.teacherchecklist.domain

import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.testTeacherCheckList
import com.anahoret.imagilabsapi.coteachers.domain.CoTeacherService
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStep.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("Update teacher check list step use case")
class UpdateTeacherCheckListStepUseCaseImplTest {

    private val teacherCheckListService = mockk<TeacherCheckListService>()
    private val classroomService = mockk<ClassroomService>()
    private val projectService = mockk<ProjectService>()
    private val coTeacherService = mockk<CoTeacherService>()
    private val updateTeacherCheckListStepUseCase = UpdateTeacherCheckListStepUseCaseImpl(teacherCheckListService, classroomService, projectService, coTeacherService)

    private val teacherId = UUID.randomUUID()

    @Test
    fun `should complete step CREATE_OR_JOIN_YOUR_FIRST_CLASSROOM when teacher has own classroom`(){
        val teacherCheckList = testTeacherCheckList(CheckListStep(CREATE_OR_JOIN_YOUR_FIRST_CLASSROOM,false))
        every { classroomService.countByTeacher(teacherId) } returns 1
        every { teacherCheckListService.completeCheckListStep(teacherId, listOf(CREATE_OR_JOIN_YOUR_FIRST_CLASSROOM)) } returns mockk()
        updateTeacherCheckListStepUseCase.update(teacherId, teacherCheckList)
        verify(exactly = 1) { teacherCheckListService.completeCheckListStep(teacherId, listOf(CREATE_OR_JOIN_YOUR_FIRST_CLASSROOM)) }
    }

    @Test
    fun `should complete step CREATE_OR_JOIN_YOUR_FIRST_CLASSROOM when teacher is a co-teacher of a classroom`(){
        val teacherCheckList = testTeacherCheckList(CheckListStep(CREATE_OR_JOIN_YOUR_FIRST_CLASSROOM,false))
        every { classroomService.countByTeacher(teacherId) } returns 0
        every { coTeacherService.isCoTeacher(teacherId) } returns true
        every { teacherCheckListService.completeCheckListStep(teacherId, listOf(CREATE_OR_JOIN_YOUR_FIRST_CLASSROOM)) } returns mockk()
        updateTeacherCheckListStepUseCase.update(teacherId, teacherCheckList)
        verify(exactly = 1) { teacherCheckListService.completeCheckListStep(teacherId, listOf(CREATE_OR_JOIN_YOUR_FIRST_CLASSROOM)) }
    }

    @Test
    fun `should not complete step CREATE_OR_JOIN_YOUR_FIRST_CLASSROOM when teacher doesn't have own classroom and isn't a co-teacher of a classroom`(){
        val teacherCheckList = testTeacherCheckList(CheckListStep(CREATE_OR_JOIN_YOUR_FIRST_CLASSROOM,false))
        every { classroomService.countByTeacher(teacherId) } returns 0
        every { coTeacherService.isCoTeacher(teacherId) } returns false
        every { teacherCheckListService.completeCheckListStep(teacherId, emptyList()) } returns mockk()
        updateTeacherCheckListStepUseCase.update(teacherId, teacherCheckList)
        verify(exactly = 0) { teacherCheckListService.completeCheckListStep(teacherId, listOf(CREATE_OR_JOIN_YOUR_FIRST_CLASSROOM)) }
    }

    @Test
    fun `should complete step CREATE_YOUR_FIRST_PROJECT as true when teacher has own project`(){
        val teacherCheckList = testTeacherCheckList(CheckListStep(CREATE_YOUR_FIRST_PROJECT,false))
        every { projectService.hasOwnProjects(teacherId) } returns true
        every { teacherCheckListService.completeCheckListStep(teacherId, listOf(CREATE_YOUR_FIRST_PROJECT)) } returns mockk()
        updateTeacherCheckListStepUseCase.update(teacherId, teacherCheckList)
        verify(exactly = 1) { teacherCheckListService.completeCheckListStep(teacherId, listOf(CREATE_YOUR_FIRST_PROJECT)) }
    }

    @Test
    fun `should not complete step CREATE_YOUR_FIRST_PROJECT as true when teacher doesn't have own project`(){
        val teacherCheckList = testTeacherCheckList(CheckListStep(CREATE_YOUR_FIRST_PROJECT,false))
        every { projectService.hasOwnProjects(teacherId) } returns false
        every { teacherCheckListService.completeCheckListStep(teacherId, emptyList()) } returns mockk()
        updateTeacherCheckListStepUseCase.update(teacherId, teacherCheckList)
        verify(exactly = 0) { teacherCheckListService.completeCheckListStep(teacherId, listOf(CREATE_YOUR_FIRST_PROJECT)) }
    }

    @Test
    fun `should not complete step SHARE_STUDENT_ACCESS_CODE as true`(){
        val teacherCheckList = testTeacherCheckList(CheckListStep(SHARE_STUDENT_ACCESS_CODE,false))
        every { projectService.hasOwnProjects(teacherId) } returns false
        every { teacherCheckListService.completeCheckListStep(teacherId, emptyList()) } returns mockk()
        updateTeacherCheckListStepUseCase.update(teacherId, teacherCheckList)
        verify(exactly = 0) { teacherCheckListService.completeCheckListStep(teacherId, listOf(SHARE_STUDENT_ACCESS_CODE)) }
    }

    @Test
    fun `should not complete step EXPLORE_YOUR_FIRST_LESSON as true`(){
        val teacherCheckList = testTeacherCheckList(CheckListStep(EXPLORE_YOUR_FIRST_LESSON,false))
        every { projectService.hasOwnProjects(teacherId) } returns false
        every { teacherCheckListService.completeCheckListStep(teacherId, emptyList()) } returns mockk()
        updateTeacherCheckListStepUseCase.update(teacherId, teacherCheckList)
        verify(exactly = 0) { teacherCheckListService.completeCheckListStep(teacherId, listOf(EXPLORE_YOUR_FIRST_LESSON)) }
    }

    @Test
    fun `should not complete step CHECK_OUT_OUR_EDUCATOR_FACEBOOK_GROUP as true`(){
        val teacherCheckList = testTeacherCheckList(CheckListStep(CHECK_OUT_OUR_EDUCATOR_FACEBOOK_GROUP,false))
        every { projectService.hasOwnProjects(teacherId) } returns false
        every { teacherCheckListService.completeCheckListStep(teacherId, emptyList()) } returns mockk()
        updateTeacherCheckListStepUseCase.update(teacherId, teacherCheckList)
        verify(exactly = 0) { teacherCheckListService.completeCheckListStep(teacherId, listOf(CHECK_OUT_OUR_EDUCATOR_FACEBOOK_GROUP)) }
    }
}
