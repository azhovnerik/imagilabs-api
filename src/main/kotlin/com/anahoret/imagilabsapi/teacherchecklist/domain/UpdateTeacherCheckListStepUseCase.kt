package com.anahoret.imagilabsapi.teacherchecklist.domain

import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.coteachers.domain.CoTeacherService
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStep
import org.springframework.stereotype.Service
import java.util.UUID

interface UpdateTeacherCheckListStepUseCase {

    fun update(teacherId: UUID, teacherCheckList: TeacherCheckList): TeacherCheckList
}

@Deprecated( "Should be deleted if all teachers check lists are created.")
@Service
class UpdateTeacherCheckListStepUseCaseImpl(
    private val teacherCheckListService: TeacherCheckListService,
    private val classroomService: ClassroomService,
    private val projectService: ProjectService,
    private val coTeacherService: CoTeacherService
) : UpdateTeacherCheckListStepUseCase {

    override fun update(teacherId: UUID, teacherCheckList: TeacherCheckList): TeacherCheckList {
        with(teacherCheckList) {
            return checkListSteps
                .filter { !it.completed && filterToComplete(teacherId, it) }
                .map { it.step }
                .let { teacherCheckListService.completeCheckListStep(teacherId, it) }
        }
    }

    private fun filterToComplete(teacherId: UUID, checkListStep: CheckListStep): Boolean {
        return when (checkListStep.step) {

            TeacherCheckListStep.CREATE_OR_JOIN_YOUR_FIRST_CLASSROOM ->
                classroomService.countByTeacher(teacherId) > 0L || coTeacherService.isCoTeacher(teacherId)

            TeacherCheckListStep.CREATE_YOUR_FIRST_PROJECT -> projectService.hasOwnProjects(teacherId)

            else -> false
        }
    }
}
