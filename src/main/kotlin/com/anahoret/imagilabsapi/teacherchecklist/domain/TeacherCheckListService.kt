package com.anahoret.imagilabsapi.teacherchecklist.domain

import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStep
import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStep.*
import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStepEntity
import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStepRepository
import org.springframework.stereotype.Service
import java.util.*

interface TeacherCheckListService {
    fun createCheckList(teacherId: UUID): TeacherCheckList
    fun getCheckList(teacherId: UUID): TeacherCheckList
    fun completeCheckListStep(teacherId: UUID, step: List<TeacherCheckListStep>): TeacherCheckList
    fun getAllNotCompleted(teacherId: UUID): TeacherCheckList
    fun completeCheckListStep(teacherId: UUID, step: TeacherCheckListStep)
    fun addTeacherCheckListStep(teacherId: UUID, step: TeacherCheckListStep, completed: Boolean)
    fun hasCompletedAllSteps(teacherId: UUID): Boolean
    fun hasCompletedAllRequiredSteps(teacherId: UUID): Boolean
    fun getCheckListStepByTeacherIdAndStep(teacherId: UUID, step: TeacherCheckListStep): CheckListStep
    fun resetCongratulationDialog(teacherId: UUID)
}

@Service
class TeacherCheckListServiceImpl(
    private val teacherCheckListRepository: TeacherCheckListStepRepository
) : TeacherCheckListService {

    override fun createCheckList(teacherId: UUID): TeacherCheckList {
        return teacherCheckListRepository.saveAll(
            listOf(
                TeacherCheckListStepEntity(teacherId, CREATE_OR_JOIN_YOUR_FIRST_CLASSROOM),
                TeacherCheckListStepEntity(teacherId, SHARE_STUDENT_ACCESS_CODE),
                TeacherCheckListStepEntity(teacherId, EXPLORE_YOUR_FIRST_LESSON),
                TeacherCheckListStepEntity(teacherId, CREATE_YOUR_FIRST_PROJECT),
                TeacherCheckListStepEntity(teacherId, CHECK_OUT_OUR_EDUCATOR_FACEBOOK_GROUP),
                TeacherCheckListStepEntity(teacherId, COMPLETE_YOUR_ACCOUNT_INFORMATION)
            )
        )
            .map(CheckListStep::mapFromEntity)
            .let(TeacherCheckList::listToTeacherCheckList)
    }

    override fun getCheckList(teacherId: UUID): TeacherCheckList {
        return teacherCheckListRepository.findAllByTeacherId(teacherId)
            .map(CheckListStep::mapFromEntity)
            .let(TeacherCheckList::listToTeacherCheckList)
    }

    override fun completeCheckListStep(teacherId: UUID, step: TeacherCheckListStep) {
        teacherCheckListRepository.findByTeacherIdAndStep(teacherId, step)
            .also { it.completed = true }
            .let(teacherCheckListRepository::save)
    }

    override fun completeCheckListStep(teacherId: UUID, step: List<TeacherCheckListStep>): TeacherCheckList {
        return teacherCheckListRepository.findAllByTeacherId(teacherId)
            .onEach { if (step.contains(it.step)) it.completed = true }
            .let(teacherCheckListRepository::saveAll)
            .map(CheckListStep::mapFromEntity)
            .let(TeacherCheckList::listToTeacherCheckList)
    }

    override fun getAllNotCompleted(teacherId: UUID): TeacherCheckList {
        return teacherCheckListRepository.findAllByTeacherIdAndCompletedIsFalse(teacherId)
            .map(CheckListStep::mapFromEntity)
            .let(TeacherCheckList::listToTeacherCheckList)
    }

    override fun addTeacherCheckListStep(teacherId: UUID, step: TeacherCheckListStep, completed: Boolean) {
        teacherCheckListRepository.save(TeacherCheckListStepEntity(teacherId, step, completed))
    }

    override fun hasCompletedAllSteps(teacherId: UUID): Boolean {
        return !teacherCheckListRepository.existsByTeacherIdAndCompletedFalse(teacherId)
    }

    override fun hasCompletedAllRequiredSteps(teacherId: UUID): Boolean {
        val allSteps = teacherCheckListRepository.findAllByTeacherId(teacherId)

        val requiredSteps = listOf(
            CREATE_OR_JOIN_YOUR_FIRST_CLASSROOM,
            SHARE_STUDENT_ACCESS_CODE,
            EXPLORE_YOUR_FIRST_LESSON,
            CREATE_YOUR_FIRST_PROJECT,
            CHECK_OUT_OUR_EDUCATOR_FACEBOOK_GROUP,
            COMPLETE_YOUR_ACCOUNT_INFORMATION
        )

        return requiredSteps.all { requiredStep ->
            allSteps.any { it.step == requiredStep && it.completed }
        }
    }

    override fun getCheckListStepByTeacherIdAndStep(teacherId: UUID, step: TeacherCheckListStep): CheckListStep {
        return teacherCheckListRepository.findByTeacherIdAndStep(teacherId, step)
            .let(CheckListStep.Companion::mapFromEntity)
    }

    override fun resetCongratulationDialog(teacherId: UUID) {
        val existing = teacherCheckListRepository.findAllByTeacherId(teacherId)
            .find { it.step == CONGRATULATION_DIALOG_SHOWN }

        if (existing == null) {
            addTeacherCheckListStep(teacherId, CONGRATULATION_DIALOG_SHOWN, completed = false)
        }
    }
}
