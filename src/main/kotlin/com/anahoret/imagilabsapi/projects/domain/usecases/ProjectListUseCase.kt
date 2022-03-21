package com.anahoret.imagilabsapi.projects.domain.usecases

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.projectclassroomshare.domain.ProjectClassroomShareService
import com.anahoret.imagilabsapi.projects.domain.ProjectCard
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import org.springframework.stereotype.Service

interface ProjectListUseCase {

    fun list(listBy: UserProfile): List<ProjectCard>
}

@Service
class ProjectListUseCaseImpl(
    private val projectService: ProjectService,
    private val projectClassroomShareService: ProjectClassroomShareService
) : ProjectListUseCase {

    override fun list(listBy: UserProfile): List<ProjectCard> {
        val projects = projectService.listByOwnerId(listBy.id)
            .takeIf { it.isNotEmpty() }
            ?: return emptyList()
        val sharedProjectIds = projectClassroomShareService.listByOwnerId(listBy.id)
            .map { it.projectId }
            .toSet()
        return projects.map { ProjectCard.fromProject(it, listBy, sharedProjectIds.contains(it.id)) }
    }
}
