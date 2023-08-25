package com.anahoret.imagilabsapi.teacherchecklist.domain

import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStep
import org.springframework.stereotype.Service
import java.util.*

interface CompleteTeacherCheckListStepUseCase {

    fun complete(teacherId: UUID, step: TeacherCheckListStep)
}

@Service
class CompleteTeacherCheckListStepUseCaseImpl(
    private val teacherCheckListService: TeacherCheckListService
): CompleteTeacherCheckListStepUseCase {

    override fun complete(teacherId: UUID, step: TeacherCheckListStep) {
        teacherCheckListService.completeCheckListStep(teacherId, step)
    }
}
