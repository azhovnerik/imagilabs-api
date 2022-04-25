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
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

interface ProjectListUseCase {
    fun list(
        listBy: UserProfile,
        searchRequest: SearchProjectsRequest,
        sort: Sort
    ): Either<OperationError, List<ProjectCard>>
}

@Service
class ProjectListUseCaseImpl(
    private val projectService: ProjectService,
    private val projectClassroomShareService: ProjectClassroomShareService,
    private val projectAccessService: ProjectAccessService,
    private val teacherProfileService: TeacherProfileService,
    private val studentProfileService: StudentProfileService
) : ProjectListUseCase {

    override fun list(
        listBy: UserProfile,
        searchRequest: SearchProjectsRequest,
        sort: Sort
    ): Either<OperationError, List<ProjectCard>> {
        return doList(listBy, searchRequest, sort)
    }

    private fun doList(
        listBy: UserProfile,
        searchRequest: SearchProjectsRequest,
        sort: Sort
    ): Either<OperationError, List<ProjectCard>> {
        val ownerId = searchRequest.ownerId
        if (!projectAccessService.canListForOwner(listBy, ownerId)) {
            return AccessDeniedError("ACCESS_TO_OWNER_PROJECTS_DENIED").left()
        }
        val projects = projectService.search(searchRequest, sort)
            .takeUnless { it.isEmpty() }
            ?: return emptyList<ProjectCard>().right()

        val owner = when (projects.first().ownerUserType) {
            UserType.TEACHER -> teacherProfileService.getTeacherById(ownerId)
            UserType.STUDENT -> studentProfileService.getStudentById(ownerId)
            UserType.ADMIN -> null
        } ?: return NotFoundError("OWNER_NOT_FOUND").left()

        val sharedProjectIds = projectClassroomShareService.listByOwnerId(ownerId)
            .map { it.projectId }
            .toSet()
        return projects.map { ProjectCard.fromProject(it, owner, sharedProjectIds.contains(it.id)) }.right()
    }

}
