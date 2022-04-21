package com.anahoret.imagilabsapi.projects.domain

import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.projectclassroomshare.domain.ProjectClassroomShare
import com.anahoret.imagilabsapi.projectclassroomshare.domain.ProjectClassroomShareService
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.stereotype.Service
import java.util.*

interface ProjectAccessService {

    fun canEdit(userProfile: UserProfile, project: Project): Boolean
    fun canRun(userProfile: UserProfile, project: Project): Boolean
    fun canShare(userProfile: UserProfile, project: Project): Boolean
    fun canUnshare(userProfile: UserProfile, project: Project): Boolean
    fun canGet(userProfile: UserProfile, project: Project): Boolean
    fun canDelete(userProfile: UserProfile, project: Project): Boolean
    fun canListForOwner(userProfile: UserProfile, ownerId: UUID): Boolean
}

@Service
class ProjectAccessServiceImpl(
    private val projectClassroomShareService: ProjectClassroomShareService,
    private val classroomService: ClassroomService,
    private val studentProfileService: StudentProfileService
) : ProjectAccessService {

    override fun canEdit(userProfile: UserProfile, project: Project): Boolean {
        return isOwner(userProfile, project) && !isShared(project)
    }

    override fun canDelete(userProfile: UserProfile, project: Project): Boolean {
        return isOwner(userProfile, project)
    }

    override fun canRun(userProfile: UserProfile, project: Project): Boolean {
        return isOwner(userProfile, project) || hasSharedAccess(userProfile, project)
    }

    override fun canShare(userProfile: UserProfile, project: Project): Boolean {
        return isOwner(userProfile, project)
    }

    override fun canUnshare(userProfile: UserProfile, project: Project): Boolean {
        return isOwner(userProfile, project) || userIsTeacherOfOwnerStudent(userProfile, project.ownerId)
    }

    override fun canGet(userProfile: UserProfile, project: Project): Boolean {
        return isOwner(userProfile, project) || hasSharedAccess(userProfile, project)
    }

    override fun canListForOwner(userProfile: UserProfile, ownerId: UUID): Boolean {
        return userProfile.id == ownerId || userIsTeacherOfOwnerStudent(userProfile, ownerId)
    }

    private fun userIsTeacherOfOwnerStudent(userProfile: UserProfile, ownerId: UUID): Boolean {
        return userProfile is TeacherProfile && studentProfileService.getStudentById(ownerId)
            ?.let { studentProfile ->
                val teacherClassroomIds = classroomService.listIdsByTeacher(userProfile.id)
                studentProfile.classroomId in teacherClassroomIds
            } ?: false
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
                val studentOwnerClassroomId = studentProfileService.getStudentById(project.ownerId)?.classroomId
                return studentOwnerClassroomId in teacherClassroomsIds
            }

            UserType.STUDENT -> {
                val projectSharedInClassroomsIds = projectClassroomShareService.getShares(project.id)
                    .map(ProjectClassroomShare::classroomId)
                    .toSet()
                val studentClassroomId = (userProfile as StudentProfile).classroomId
                return studentClassroomId in projectSharedInClassroomsIds
            }

            else -> false
        }
    }

}
