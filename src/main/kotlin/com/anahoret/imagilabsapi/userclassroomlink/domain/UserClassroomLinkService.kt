package com.anahoret.imagilabsapi.userclassroomlink.domain

import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.coteachers.domain.CoTeacherService
import com.anahoret.imagilabsapi.students.domain.StudentProfile
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
    private val coTeacherService: CoTeacherService
) : UserClassroomLinkService {

    override fun isLinkedToClassroom(userProfile: UserProfile, classroomId: UUID): Boolean {
        return when (userProfile.userType) {
            UserType.TEACHER -> classroomService.isClassroomOwnedByTeacher(classroomId, userProfile.id)
            UserType.STUDENT -> isStudentLinkedToClassroom(userProfile, classroomId)
            else -> false
        }
    }

    override fun isLinkedToClassroom(userProfile: UserProfile, classroom: Classroom): Boolean {
        return when (userProfile.userType) {
            UserType.TEACHER -> classroom.teacherId == userProfile.id || coTeacherService.isCoClassroom(classroom.id, userProfile.id)
            UserType.STUDENT -> isStudentLinkedToClassroom(userProfile, classroom.id)
            else -> false
        }
    }

    private fun isStudentLinkedToClassroom(userProfile: UserProfile, classroomId: UUID): Boolean {
        return userProfile is StudentProfile && userProfile.classroomId == classroomId
    }

}
