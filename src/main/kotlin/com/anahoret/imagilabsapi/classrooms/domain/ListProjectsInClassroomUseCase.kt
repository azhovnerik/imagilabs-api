package com.anahoret.imagilabsapi.classrooms.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.projectclassroomshare.domain.ProjectClassroomShare
import com.anahoret.imagilabsapi.projectclassroomshare.domain.ProjectClassroomShareService
import com.anahoret.imagilabsapi.projects.domain.Project
import com.anahoret.imagilabsapi.projects.domain.ProjectCard
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.*

interface ListProjectsInClassroomUseCase {
    fun list(
        listBy: UserProfile,
        classroomId: UUID,
        searchRequest: ClassroomSearchProjectsRequest,
        sort: Sort
    ): Either<OperationError, List<ProjectCard>>
}

@Service
class ListProjectsInClassroomUseCaseImpl(
    private val projectClassroomShareService: ProjectClassroomShareService,
    private val projectService: ProjectService,
    private val classroomService: ClassroomService,
    private val classroomAccessService: ClassroomAccessService,
    private val teacherProfileService: TeacherProfileService,
    private val studentProfileService: StudentProfileService
) : ListProjectsInClassroomUseCase {

    override fun list(
        listBy: UserProfile,
        classroomId: UUID,
        searchRequest: ClassroomSearchProjectsRequest,
        sort: Sort
    ): Either<OperationError, List<ProjectCard>> {
        return doList(listBy, classroomId, searchRequest, sort)
    }

    private fun doList(
        listBy: UserProfile,
        classroomId: UUID,
        searchRequest: ClassroomSearchProjectsRequest,
        sort: Sort
    ): Either<OperationError, List<ProjectCard>> {
        val classroom = classroomService.getById(classroomId) ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()
        if (!classroomAccessService.canListProjects(listBy, classroom))
            return AccessDeniedError("ACCESS_TO_CLASSROOM_PROJECTS_LIST_DENIED").left()

        val projects = projectClassroomShareService.listByClassroom(classroom.id, searchRequest)
            .map(ProjectClassroomShare::projectId)
            .let { projectService.listByIds(it, sort) }

        val teachers = projects
            .filter { it.ownerUserType == UserType.TEACHER }
            .map(Project::ownerId)
            .let(teacherProfileService::listByIds)

        val students = projects
            .filter { it.ownerUserType == UserType.STUDENT }
            .map(Project::ownerId)
            .let(studentProfileService::listByIds)

        val owners = (students + teachers).associateBy(UserProfile::id)

        return projects.map {
            ProjectCard.fromProject(it, owners.getValue(it.ownerId), shared = true)
        }.right()
    }


}
