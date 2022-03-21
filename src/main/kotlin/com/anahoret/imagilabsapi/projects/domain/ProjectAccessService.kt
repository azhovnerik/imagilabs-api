package com.anahoret.imagilabsapi.projects.domain

import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.projectclassroomshare.domain.ProjectClassroomShare
import com.anahoret.imagilabsapi.projectclassroomshare.domain.ProjectClassroomShareService
import com.anahoret.imagilabsapi.studentclassroomlink.domain.StudentClassroomLinkService
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.stereotype.Service

interface ProjectAccessService {

    fun canEdit(userProfile: UserProfile, project: Project): Boolean
    fun canRun(userProfile: UserProfile, project: Project): Boolean
    fun canShare(userProfile: UserProfile, project: Project): Boolean
    fun canUnshare(userProfile: UserProfile, project: Project): Boolean
    fun canGet(userProfile: UserProfile, project: Project): Boolean
}

@Service
class ProjectAccessServiceImpl(
    private val studentClassroomLinkService: StudentClassroomLinkService,
    private val projectClassroomShareService: ProjectClassroomShareService,
    private val classroomService: ClassroomService
) : ProjectAccessService {

    override fun canEdit(userProfile: UserProfile, project: Project): Boolean {
        return isOwner(userProfile, project) && !isShared(project)
    }

    override fun canRun(userProfile: UserProfile, project: Project): Boolean {
        return isOwner(userProfile, project) || hasSharedAccess(userProfile, project)
    }

    override fun canShare(userProfile: UserProfile, project: Project): Boolean {
        return isOwner(userProfile, project)
    }

    override fun canUnshare(userProfile: UserProfile, project: Project): Boolean {
        return isOwner(userProfile, project)
    }

    override fun canGet(userProfile: UserProfile, project: Project): Boolean {
        return isOwner(userProfile, project) || hasSharedAccess(userProfile, project)
    }

    private fun isOwner(userProfile: UserProfile, project: Project): Boolean {
        return project.ownerId == userProfile.id && project.ownerUserType == userProfile.userType
    }

    private fun isShared(project: Project): Boolean {
        return projectClassroomShareService.getShares(project.id).isNotEmpty()
    }

    private fun hasSharedAccess(userProfile: UserProfile, project: Project): Boolean {
        return when (userProfile.userType) {
            UserType.TEACHER -> {
                val teacherClassroomsIds = classroomService.listByTeacher(userProfile.id)
                    .map { it.id }
                    .toSet()
                return studentClassroomLinkService.getLinks(project.ownerId)
                    .any { it.classroomId in teacherClassroomsIds }
            }

            UserType.STUDENT -> {
                val projectSharedInClassroomsIds = projectClassroomShareService.getShares(project.id)
                    .map(ProjectClassroomShare::classroomId)
                    .toSet()
                studentClassroomLinkService.getLinks(userProfile.id)
                    .any { classroomLink -> classroomLink.classroomId in projectSharedInClassroomsIds }
            }
        }
    }

}
