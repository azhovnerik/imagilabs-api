package com.anahoret.imagilabsapi.projects.domain

import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.coteachers.domain.CoTeacherService
import com.anahoret.imagilabsapi.projectclassroomshare.domain.ProjectClassroomShare
import com.anahoret.imagilabsapi.projectclassroomshare.domain.ProjectClassroomShareService
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.stereotype.Service
import java.util.*

interface ProjectAccessService {

    fun canEdit(userProfile: UserProfile, project: Project, classroomId: UUID?): Boolean
    fun canRun(userProfile: UserProfile, project: Project, classroomId: UUID): Boolean
    fun canShare(userProfile: UserProfile, project: Project, classroomIds: List<UUID>): Boolean
    fun canUnshare(userProfile: UserProfile, project: Project, classroomId: UUID?): Boolean
    fun canUnshare(userProfile: UserProfile, project: Project, classroomIds: List<UUID>): Boolean
    fun canGet(userProfile: UserProfile, project: Project, classroomId: UUID?): Boolean
    fun canDelete(userProfile: UserProfile, project: Project, classroomId: UUID?): Boolean
    fun canListForOwner(userProfile: UserProfile, ownerId: UUID, classroomId: UUID?): Boolean
}

@Service
class ProjectAccessServiceImpl(
    private val projectClassroomShareService: ProjectClassroomShareService,
    private val classroomService: ClassroomService,
    private val studentProfileService: StudentProfileService,
    private val coTeacherService: CoTeacherService,
) : ProjectAccessService {

    override fun canEdit(userProfile: UserProfile, project: Project, classroomId: UUID?): Boolean {
        return isProjectOwner(userProfile, project) && !isShared(project)
    }

    override fun canDelete(userProfile: UserProfile, project: Project, classroomId: UUID?): Boolean {
        return isProjectOwner(userProfile, project) || isCoTeacher(userProfile, classroomId)
    }

    override fun canRun(userProfile: UserProfile, project: Project, classroomId: UUID): Boolean {
        return (isProjectOwner(userProfile, project) || isCoTeacher(userProfile, classroomId))
                || hasSharedAccess(userProfile, project) || isOwnerOfClassroom(classroomId, userProfile)
    }

    override fun canShare(userProfile: UserProfile, project: Project, classroomIds: List<UUID>): Boolean {
        return isProjectOwner(userProfile, project) || isOwnerOrCoTeacher(userProfile, classroomIds)
    }

    override fun canUnshare(userProfile: UserProfile, project: Project, classroomId: UUID?): Boolean {
        return isProjectOwner(userProfile, project) || isCoTeacher(userProfile, classroomId)
                || userIsTeacherOfOwnerStudent(userProfile, project.ownerId)
                || isOwnerOfClassroom(classroomId, userProfile)
    }

    override fun canUnshare(userProfile: UserProfile, project: Project, classroomIds: List<UUID>): Boolean {
        return isProjectOwner(userProfile, project) || isOwnerOrCoTeacher(userProfile, classroomIds)
                || userIsTeacherOfOwnerStudent(userProfile, project.ownerId)
    }

    override fun canGet(userProfile: UserProfile, project: Project, classroomId: UUID?): Boolean {
        return isProjectOwner(userProfile, project) || hasSharedAccess(userProfile, project)
                || isCoTeacher(userProfile, classroomId) || isOwnerOfClassroom(classroomId, userProfile)
    }

    override fun canListForOwner(userProfile: UserProfile, ownerId: UUID, classroomId: UUID?): Boolean {
        return userProfile.id == ownerId || userIsTeacherOfOwnerStudent(userProfile, ownerId)
                || isCoTeacher(userProfile, classroomId)
    }

    private fun userIsTeacherOfOwnerStudent(userProfile: UserProfile, ownerId: UUID): Boolean {
        return userProfile is TeacherProfile && studentProfileService.getStudentById(ownerId)
            ?.let { studentProfile ->
                val teacherClassroomIds = classroomService.listIdsByTeacher(userProfile.id)
                studentProfile.classroomId in teacherClassroomIds
            } ?: false
    }

    private fun isProjectOwner(userProfile: UserProfile, project: Project): Boolean {
        return project.ownerId == userProfile.id && project.ownerUserType == userProfile.userType
    }

    private fun isCoTeacher(userProfile: UserProfile, classroomId: UUID?): Boolean {
        return classroomId != null && coTeacherService.isLinkedToClassroom(classroomId, userProfile.id)
    }

    private fun isOwnerOrCoTeacher(userProfile: UserProfile, classroomIds: List<UUID>): Boolean {
        val coTeacherClassroomIds = coTeacherService.getClassroomIdListByTeacherId(userProfile.id)
        val teacherClassroomIds = classroomService.listIdsByTeacher(userProfile.id)

        return coTeacherClassroomIds.any { classroomIds.contains(it) }
                || teacherClassroomIds.any { classroomIds.contains(it) }
    }

    private fun isOwnerOfClassroom(classroomId: UUID?, userProfile: UserProfile): Boolean {
        return classroomId != null && classroomService.isClassroomOwnedByTeacher(classroomId, userProfile.id)
    }

    private fun isShared(project: Project): Boolean {
        return projectClassroomShareService.getShares(project.id).isNotEmpty()
    }

    private fun hasSharedAccess(userProfile: UserProfile, project: Project): Boolean {
        return when (userProfile.userType) {
            UserType.TEACHER -> {
                val teacherClassroomsIds = classroomService.listIdsByTeacher(userProfile.id)
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
