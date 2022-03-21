package com.anahoret.imagilabsapi.userclassroomlink.domain

import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.stereotype.Service
import java.util.*

interface UserClassroomLinkService {

    fun isLinkedToClassroom(userProfile: UserProfile, classroomId: UUID): Boolean
    fun isLinkedToClassroom(userProfile: UserProfile, classroom: Classroom): Boolean
}

@Service
class UserClassroomLinkServiceImpl(
    private val classroomService: ClassroomService,
    private val studentClassroomLinkService: StudentClassroomLinkService
) : UserClassroomLinkService {

    override fun isLinkedToClassroom(userProfile: UserProfile, classroomId: UUID): Boolean {
        return when (userProfile.userType) {
            UserType.TEACHER -> classroomService.isClassroomOwnedByTeacher(classroomId, userProfile.id)
            UserType.STUDENT -> studentClassroomLinkService.isStudentLinkedToClassroom(userProfile.id, classroomId)
        }
    }

    override fun isLinkedToClassroom(userProfile: UserProfile, classroom: Classroom): Boolean {
        return when (userProfile.userType) {
            UserType.TEACHER -> classroom.teacherId == userProfile.id
            UserType.STUDENT -> studentClassroomLinkService.isStudentLinkedToClassroom(userProfile.id, classroom.id)
        }
    }

}
