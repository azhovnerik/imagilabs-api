package com.anahoret.imagilabsapi.projects.domain.usecases

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.projects.domain.ProjectDetails
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import com.anahoret.imagilabsapi.teacherchecklist.domain.CompleteTeacherCheckListStepUseCase
import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStep.CREATE_YOUR_FIRST_PROJECT
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.stereotype.Service

interface ProjectCreateUseCase {

    fun create(userProfile: UserProfile): ProjectDetails
}

@Service
class ProjectCreateUseCaseImpl(
    private val projectService: ProjectService,
    private val completeTeacherCheckListStepUseCase: CompleteTeacherCheckListStepUseCase
) : ProjectCreateUseCase {

    override fun create(userProfile: UserProfile): ProjectDetails {
        val project = projectService.createProject(userProfile.id, userProfile.userType)

        if (userProfile.userType == UserType.TEACHER)
            completeTeacherCheckListStepUseCase.complete(userProfile.id, CREATE_YOUR_FIRST_PROJECT)

        return ProjectDetails.fromProject(
            project,
            owner = userProfile,
            canEdit = true,
            canUnshare = true,
            classroomShares = emptyList()
        )
    }

}
