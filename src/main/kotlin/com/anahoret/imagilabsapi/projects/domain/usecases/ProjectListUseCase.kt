package com.anahoret.imagilabsapi.projects.domain.usecases

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.projectclassroomshare.domain.ProjectClassroomShareService
import com.anahoret.imagilabsapi.projects.domain.ListProjectsRequest
import com.anahoret.imagilabsapi.projects.domain.ProjectAccessService
import com.anahoret.imagilabsapi.projects.domain.ProjectCard
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.*

interface ProjectListUseCase {

    fun list(listBy: UserProfile, ownerId: UUID?, pageable: Pageable): Either<OperationError, Page<ProjectCard>>
    fun list(
        listBy: UserProfile,
        listRequest: ListProjectsRequest,
        pageable: Pageable
    ): Either<OperationError, Page<ProjectCard>>
}

@Service
class ProjectListUseCaseImpl(
    private val projectService: ProjectService,
    private val projectClassroomShareService: ProjectClassroomShareService,
    private val projectAccessService: ProjectAccessService
) : ProjectListUseCase {

    override fun list(
        listBy: UserProfile,
        ownerId: UUID?,
        pageable: Pageable,
    ): Either<OperationError, Page<ProjectCard>> {
        return doList(listBy, ListProjectsRequest(ownerId, null, null), pageable)
    }

    override fun list(
        listBy: UserProfile,
        listRequest: ListProjectsRequest,
        pageable: Pageable
    ): Either<OperationError, Page<ProjectCard>> {
        return doList(listBy, listRequest, pageable)
    }

    private fun doList(
        listBy: UserProfile,
        listRequest: ListProjectsRequest,
        pageable: Pageable
    ): Either<OperationError, Page<ProjectCard>> {
        val ownerId = listRequest.ownerId ?: listBy.id
        if (!projectAccessService.canListForOwner(listBy, ownerId)) {
            return AccessDeniedError("ACCESS_TO_OWNER_PROJECTS_DENIED").left()
        }
        val projects = projectService.listByOwnerId(listBy.id, pageable)
            .takeUnless { it.isEmpty }
            ?: return Page.empty<ProjectCard>().right()
        val sharedProjectIds = projectClassroomShareService.listByOwnerId(listBy.id)
            .map { it.projectId }
            .toSet()
        return projects.map { ProjectCard.fromProject(it, listBy, sharedProjectIds.contains(it.id)) }.right()
    }

}
