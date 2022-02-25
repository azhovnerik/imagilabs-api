package com.anahoret.imagilabsapi.projects.domain

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
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
