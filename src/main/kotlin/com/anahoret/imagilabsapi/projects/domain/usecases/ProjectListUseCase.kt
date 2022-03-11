package com.anahoret.imagilabsapi.projects.domain.usecases

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.projects.domain.Project
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import org.springframework.stereotype.Service

interface ProjectListUseCase {

    fun list(listBy: UserProfile): List<Project>
}

@Service
class ProjectListUseCaseImpl(
    private val projectService: ProjectService
) : ProjectListUseCase {

    override fun list(listBy: UserProfile): List<Project> {
        return projectService.listByOwnerId(listBy.id)
    }
}
