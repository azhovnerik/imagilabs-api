package com.anahoret.imagilabsapi.classrooms.domain

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.userclassroomlink.domain.CoTeacherClassroomLinkService
import com.anahoret.imagilabsapi.userclassroomlink.domain.UserClassroomLinkService
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.stereotype.Service

interface ClassroomAccessService {

    fun canListProjects(userProfile: UserProfile, classroom: Classroom): Boolean
    fun canGetClassroom(userProfile: UserProfile, classroom: Classroom): Boolean
    fun canListStudentCredentials(userProfile: UserProfile, classroom: Classroom): Boolean
    fun canDeleteClassroom(userProfile: UserProfile, classroom: Classroom): Boolean
    fun canUpdateClassroom(userProfile: UserProfile, classroom: Classroom): Boolean
    fun canGetTeachingMaterials(userProfile: UserProfile, classroom: Classroom): Boolean
}

@Service
class ClassroomAccessServiceImpl(
    private val userClassroomLinkService: UserClassroomLinkService,
    private val coTeacherClassroomLinkService: CoTeacherClassroomLinkService
) : ClassroomAccessService {

    override fun canListProjects(userProfile: UserProfile, classroom: Classroom): Boolean {
        return userClassroomLinkService.isLinkedToClassroom(userProfile, classroom)
                || coTeacherClassroomLinkService.hasAccessToClassroom(classroom.id, userProfile)
    }

    override fun canGetClassroom(userProfile: UserProfile, classroom: Classroom): Boolean {
        return userClassroomLinkService.isLinkedToClassroom(userProfile, classroom)
                || coTeacherClassroomLinkService.hasAccessToClassroom(classroom.id, userProfile)
    }

    override fun canListStudentCredentials(userProfile: UserProfile, classroom: Classroom): Boolean {
        return when (userProfile.userType) {
            UserType.TEACHER -> userClassroomLinkService.isLinkedToClassroom(userProfile, classroom)
                    || coTeacherClassroomLinkService.hasAccessToClassroom(classroom.id, userProfile)

            else -> false
        }
    }

    override fun canDeleteClassroom(userProfile: UserProfile, classroom: Classroom): Boolean {
        return when (userProfile.userType) {
            UserType.TEACHER -> classroom.teacherId == userProfile.id
            else -> false
        }
    }

    override fun canUpdateClassroom(userProfile: UserProfile, classroom: Classroom): Boolean {
        return when (userProfile.userType) {
            UserType.TEACHER -> classroom.teacherId == userProfile.id
                    || coTeacherClassroomLinkService.hasAccessToClassroom(classroom.id, userProfile)

            else -> false
        }
    }

    override fun canGetTeachingMaterials(userProfile: UserProfile, classroom: Classroom): Boolean {
        return userClassroomLinkService.isLinkedToClassroom(userProfile, classroom)
                || coTeacherClassroomLinkService.hasAccessToClassroom(classroom.id, userProfile)
    }

}
