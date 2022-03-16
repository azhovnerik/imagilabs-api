package com.anahoret.imagilabsapi.projects.domain.usecases

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.projects.domain.ProjectCard
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import org.springframework.stereotype.Service

interface ProjectListUseCase {

    fun list(listBy: UserProfile): List<ProjectCard>
}

@Service
class ProjectListUseCaseImpl(
    private val projectService: ProjectService
) : ProjectListUseCase {

    override fun list(listBy: UserProfile): List<ProjectCard> {
        return projectService.listByOwnerId(listBy.id)
            .map { ProjectCard.fromProject(it, listBy) }
    }
}
