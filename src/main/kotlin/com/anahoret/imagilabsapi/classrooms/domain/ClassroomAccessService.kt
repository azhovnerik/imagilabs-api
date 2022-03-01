package com.anahoret.imagilabsapi.classrooms.domain

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.studentclassroomlink.domain.StudentClassroomLinkService
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.stereotype.Service

interface ClassroomAccessService {

    fun canListProjects(userProfile: UserProfile, classroom: Classroom): Boolean
}

@Service
class ClassroomAccessServiceImpl(
    private val studentClassroomLinkService: StudentClassroomLinkService
) : ClassroomAccessService {

    override fun canListProjects(userProfile: UserProfile, classroom: Classroom): Boolean {
        return when (userProfile.userType) {
            UserType.TEACHER -> userProfile.id == classroom.teacherId
            UserType.STUDENT -> studentClassroomLinkService.isStudentLinkedToClassroom(userProfile.id, classroom.id)
        }
    }

}
