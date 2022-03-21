package com.anahoret.imagilabsapi.classrooms.domain

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.userclassroomlink.domain.StudentClassroomLinkService
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.stereotype.Service

interface ClassroomAccessService {

    fun canListProjects(userProfile: UserProfile, classroom: Classroom): Boolean
    fun canGetClassroom(userProfile: UserProfile, classroom: Classroom): Boolean
    fun canListStudentCredentials(userProfile: UserProfile, classroom: Classroom): Boolean
}

@Service
class ClassroomAccessServiceImpl(
    private val studentClassroomLinkService: StudentClassroomLinkService
) : ClassroomAccessService {

    override fun canListProjects(userProfile: UserProfile, classroom: Classroom): Boolean {
        return when (userProfile.userType) {
            UserType.TEACHER -> isClassroomTeacher(userProfile, classroom)
            UserType.STUDENT -> isClassroomStudent(userProfile, classroom)
        }
    }

    override fun canGetClassroom(userProfile: UserProfile, classroom: Classroom): Boolean {
        return when (userProfile.userType) {
            UserType.TEACHER -> isClassroomTeacher(userProfile, classroom)
            UserType.STUDENT -> isClassroomStudent(userProfile, classroom)
        }
    }

    override fun canListStudentCredentials(userProfile: UserProfile, classroom: Classroom): Boolean {
        return when (userProfile.userType) {
            UserType.TEACHER -> isClassroomTeacher(userProfile, classroom)
            UserType.STUDENT -> false
        }
    }

    private fun isClassroomStudent(userProfile: UserProfile, classroom: Classroom): Boolean {
        return studentClassroomLinkService.isStudentLinkedToClassroom(userProfile.id, classroom.id)
    }

    private fun isClassroomTeacher(userProfile: UserProfile, classroom: Classroom): Boolean {
        return userProfile.id == classroom.teacherId
    }

}
