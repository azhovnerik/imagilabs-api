package com.anahoret.imagilabsapi.teacherchecklist.domain

import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStep.CONGRATULATION_DIALOG_SHOWN
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.stereotype.Service

interface CompleteTeacherCheckListUseCase {

    fun completeTeacherCheckList(teacherProfile: TeacherProfile)

}

@Service
class CompleteTeacherCheckListUseCaseImpl(
    private val teacherCheckListService: TeacherCheckListService
) : CompleteTeacherCheckListUseCase {

    override fun completeTeacherCheckList(teacherProfile: TeacherProfile) {
        teacherCheckListService.addTeacherCheckListStep(
            teacherId = teacherProfile.id,
            step = CONGRATULATION_DIALOG_SHOWN,
            completed = true
        )
    }
}
