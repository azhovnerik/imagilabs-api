package com.anahoret.imagilabsapi.classrooms.domain

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.userclassroomlink.domain.UserClassroomLinkService
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.stereotype.Service

interface ClassroomAccessService {

    fun canListProjects(userProfile: UserProfile, classroom: Classroom): Boolean
    fun canGetClassroom(userProfile: UserProfile, classroom: Classroom): Boolean
    fun canListStudentCredentials(userProfile: UserProfile, classroom: Classroom): Boolean
}

@Service
class ClassroomAccessServiceImpl(
    private val userClassroomLinkService: UserClassroomLinkService
) : ClassroomAccessService {

    override fun canListProjects(userProfile: UserProfile, classroom: Classroom): Boolean {
        return userClassroomLinkService.isLinkedToClassroom(userProfile, classroom)
    }

    override fun canGetClassroom(userProfile: UserProfile, classroom: Classroom): Boolean {
        return userClassroomLinkService.isLinkedToClassroom(userProfile, classroom)
    }

    override fun canListStudentCredentials(userProfile: UserProfile, classroom: Classroom): Boolean {
        return when (userProfile.userType) {
            UserType.TEACHER -> userClassroomLinkService.isLinkedToClassroom(userProfile, classroom)
            UserType.STUDENT -> false
        }
    }

}
