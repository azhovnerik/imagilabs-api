package com.anahoret.imagilabsapi.teacherchecklist.domain

import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStep.COMPLETE_YOUR_ACCOUNT_INFORMATION
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.stereotype.Service

interface CompleteAccountInformationCheckListStepUseCase {
    fun checkAndComplete(teacherProfile: TeacherProfile)
}

@Service
class CompleteAccountInformationCheckListStepUseCaseImpl(
    private val completeTeacherCheckListStepUseCase: CompleteTeacherCheckListStepUseCase
) : CompleteAccountInformationCheckListStepUseCase {

    override fun checkAndComplete(teacherProfile: TeacherProfile) {
        if (isProfileComplete(teacherProfile)) {
            completeTeacherCheckListStepUseCase.complete(teacherProfile.id, COMPLETE_YOUR_ACCOUNT_INFORMATION)
        }
    }

    private fun isProfileComplete(profile: TeacherProfile): Boolean {
        return profile.firstName.isNotBlank() &&
            profile.lastName.isNotBlank() &&
            profile.email.isNotBlank() &&
            profile.country.isNotBlank() &&
            profile.organization.isNotBlank() &&
            profile.schoolRoles.isNotEmpty() &&
            profile.grades.isNotEmpty() &&
            !profile.subjects.isNullOrBlank() &&
            !profile.schools.isNullOrBlank()
    }
}
