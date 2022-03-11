package com.anahoret.imagilabsapi.projectclassroomshare.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.projects.domain.ProjectAccessService
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import com.anahoret.imagilabsapi.studentclassroomlink.domain.StudentClassroomLinkService
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.stereotype.Service
import java.util.*

interface ProjectClassroomShareUseCase {

    fun share(
        sharedBy: UserProfile,
        projectClassroomShareRequest: ProjectClassroomShareRequest
    ): Either<OperationError, Unit>
}

@Service
class ProjectClassroomShareUseCaseImpl(
    private val projectClassroomShareService: ProjectClassroomShareService,
    private val projectService: ProjectService,
    private val classroomService: ClassroomService,
    private val studentClassroomLinkService: StudentClassroomLinkService,
    private val projectAccessService: ProjectAccessService
) : ProjectClassroomShareUseCase {

    override fun share(
        sharedBy: UserProfile,
        projectClassroomShareRequest: ProjectClassroomShareRequest
    ): Either<OperationError, Unit> {
        val projectId = projectClassroomShareRequest.projectId
        val classroomIds = projectClassroomShareRequest.classroomIds

        val project = projectService.getProjectById(projectId)
            ?: return NotFoundError("PROJECT_NOT_FOUND").left()

        if (!projectAccessService.canShare(sharedBy, project))
            return AccessDeniedError("ACCESS_TO_PROJECT_DENIED").left()

        if (classroomIds.any { !isLinkedToClassroom(sharedBy, it) })
            return AccessDeniedError("ACCESS_TO_CLASSROOM_DENIED").left()

        projectClassroomShareService.shareToAll(projectId, classroomIds)
        return Unit.right()
    }

    private fun isLinkedToClassroom(userProfile: UserProfile, classroomId: UUID): Boolean {
        return when (userProfile.userType) {
            UserType.TEACHER -> classroomService.isClassroomOwnedByTeacher(classroomId, userProfile.id)
            UserType.STUDENT -> studentClassroomLinkService.isStudentLinkedToClassroom(userProfile.id, classroomId)
        }
    }

}
