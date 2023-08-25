package com.anahoret.imagilabsapi.teacherchecklist.domain

import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStep
import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStepEntity
import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStepRepository
import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStep.*
import org.springframework.stereotype.Service
import java.util.UUID

interface TeacherCheckListService {
    fun createCheckList(teacherId: UUID): TeacherCheckList
    fun getCheckList(teacherId: UUID): TeacherCheckList
    fun completeCheckListStep(teacherId: UUID, step: List<TeacherCheckListStep>): TeacherCheckList
    fun getAllNotCompleted(teacherId: UUID): TeacherCheckList
    fun completeCheckListStep(teacherId: UUID, step: TeacherCheckListStep)
    fun addTeacherCheckListStep(teacherId: UUID, step: TeacherCheckListStep, completed: Boolean)
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
                TeacherCheckListStepEntity(teacherId, CHECK_OUT_OUR_EDUCATOR_FACEBOOK_GROUP)
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
            .let {
                it.completed = true
                it
            }
            .let(teacherCheckListRepository::save)
    }

    override fun completeCheckListStep(teacherId: UUID, step: List<TeacherCheckListStep>): TeacherCheckList {
        return teacherCheckListRepository.findAllByTeacherId(teacherId)
            .map {
                if (step.contains(it.step)) it.completed = true;
                it
            }
            .let { teacherCheckListRepository.saveAll(it) }
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
}
