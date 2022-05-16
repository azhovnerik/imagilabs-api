package com.anahoret.imagilabsapi.projects.domain.usecases

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.projects.domain.ProjectDetails
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import org.springframework.stereotype.Service

interface ProjectCreateUseCase {

    fun create(userProfile: UserProfile): ProjectDetails
}

@Service
class ProjectCreateUseCaseImpl(
    private val projectService: ProjectService
) : ProjectCreateUseCase {

    override fun create(userProfile: UserProfile): ProjectDetails {
        val project = projectService.createProject(userProfile.id, userProfile.userType)
        return ProjectDetails.fromProject(
            project,
            owner = userProfile,
            canEdit = true,
            canUnshare = true,
            classroomShares = emptyList()
        )
    }

}
