package com.anahoret.imagilabsapi.teacherchecklist.domain

import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStepEntity
import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStep

@Suppress("unused")
class TeacherCheckList (
    val checkListSteps: List<CheckListStep>
) {

    companion object {

        fun listToTeacherCheckList(checkListSteps: List<CheckListStep>): TeacherCheckList {
            return TeacherCheckList(checkListSteps)
        }
    }
}

@Suppress("unused")
class CheckListStep(
    val step: TeacherCheckListStep,
    val completed: Boolean
) {

    companion object {

        fun mapFromEntity(entity: TeacherCheckListStepEntity): CheckListStep {
            return with(entity) { CheckListStep(step, completed) }
        }    }
}
