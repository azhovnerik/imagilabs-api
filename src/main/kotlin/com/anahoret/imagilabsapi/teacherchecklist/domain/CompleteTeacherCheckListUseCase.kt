package com.anahoret.imagilabsapi.teacherchecklist.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStep.CONGRATULATION_DIALOG_SHOWN
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.stereotype.Service

interface CompleteTeacherCheckListUseCase {

    fun completeTeacherCheckList(teacherProfile: TeacherProfile): Either<OperationError, Unit>
}

@Service
class CompleteTeacherCheckListUseCaseImpl(
    private val teacherCheckListService: TeacherCheckListService
) : CompleteTeacherCheckListUseCase {

    override fun completeTeacherCheckList(teacherProfile: TeacherProfile): Either<OperationError, Unit> {

        if (!teacherCheckListService.hasCompletedAllRequiredSteps(teacherProfile.id))
            return ValidationError("TEACHER_CHECK_LIST_SHOULD_BE_COMPLETED").left()

        return teacherCheckListService.addTeacherCheckListStep(
            teacherId = teacherProfile.id,
            step = CONGRATULATION_DIALOG_SHOWN,
            completed = true
        ).right()
    }
}
