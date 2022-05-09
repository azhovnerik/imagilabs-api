package com.anahoret.imagilabsapi.projects.domain.usecases

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.projectclassroomshare.domain.ProjectClassroomShareService
import com.anahoret.imagilabsapi.projects.domain.ProjectAccessService
import com.anahoret.imagilabsapi.projects.domain.ProjectCard
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import com.anahoret.imagilabsapi.projects.domain.SearchProjectsRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

interface ProjectListUseCase {

    fun list(
        listBy: UserProfile,
        searchRequest: SearchProjectsRequest,
        pageable: Pageable
    ): Either<OperationError, Page<ProjectCard>>
}

@Service
class ProjectListUseCaseImpl(
    private val projectService: ProjectService,
    private val projectClassroomShareService: ProjectClassroomShareService,
    private val projectAccessService: ProjectAccessService,
    private val projectOwnerGetUseCase: ProjectOwnerGetUseCase
) : ProjectListUseCase {

    override fun list(
        listBy: UserProfile,
        searchRequest: SearchProjectsRequest,
        pageable: Pageable
    ): Either<OperationError, Page<ProjectCard>> {
        val ownerId = searchRequest.ownerId
        if (!projectAccessService.canListForOwner(listBy, ownerId)) {
            return AccessDeniedError("ACCESS_TO_OWNER_PROJECTS_DENIED").left()
        }
        val projects = projectService.search(searchRequest, pageable)
            .takeUnless { it.isEmpty }
            ?: return Page.empty<ProjectCard>().right()

        val owner = projectOwnerGetUseCase.get(projects.first())
            ?: return NotFoundError("OWNER_NOT_FOUND").left()

        val sharedProjectIds = projectClassroomShareService.listByOwnerId(ownerId)
            .map { it.projectId }
            .toSet()
        return projects.map { ProjectCard.fromProject(it, owner, sharedProjectIds.contains(it.id)) }.right()
    }

}
