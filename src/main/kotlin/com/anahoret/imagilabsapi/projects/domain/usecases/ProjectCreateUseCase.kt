package com.anahoret.imagilabsapi.projects.domain.usecases

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.projects.domain.Project
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import org.springframework.stereotype.Service

interface ProjectCreateUseCase {

    fun create(userProfile: UserProfile): Project
}

@Service
class ProjectCreateUseCaseImpl(
    private val projectService: ProjectService
) : ProjectCreateUseCase {

    override fun create(userProfile: UserProfile): Project {
        return projectService.createProject(userProfile.id, userProfile.userType)
    }

}
