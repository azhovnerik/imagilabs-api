package com.anahoret.imagilabsapi.students.domain

import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.coteachers.domain.CoTeacherService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.stereotype.Service
import java.util.*

interface StudentAccessService {

    fun canDelete(teacherProfile: TeacherProfile, studentProfile: StudentProfile): Boolean
    fun canUpdate(teacherProfile: TeacherProfile, studentProfile: StudentProfile): Boolean
    fun canGet(userProfile: UserProfile, studentDetails: StudentDetails): Boolean
}

@Service
class StudentAccessServiceImpl(
    private val classroomService: ClassroomService,
    private val coTeacherService: CoTeacherService
) : StudentAccessService {

    override fun canDelete(teacherProfile: TeacherProfile, studentProfile: StudentProfile): Boolean {
        return studentIsInTeacherClassroom(studentProfile.classroomId, teacherProfile)
                || isLikedAsCoTeacher(studentProfile.classroomId, teacherProfile.id)
    }

    override fun canUpdate(teacherProfile: TeacherProfile, studentProfile: StudentProfile): Boolean {
        return studentIsInTeacherClassroom(studentProfile.classroomId, teacherProfile)
                || isLikedAsCoTeacher(studentProfile.classroomId, teacherProfile.id)
    }

    override fun canGet(userProfile: UserProfile, studentDetails: StudentDetails): Boolean {
        return when (userProfile.userType) {
            UserType.TEACHER -> studentIsInTeacherClassroom(studentDetails.classroomId, userProfile as TeacherProfile)
                    || isLikedAsCoTeacher(studentDetails.classroomId, userProfile.id)
            UserType.STUDENT -> userProfile.id == studentDetails.id
            UserType.ADMIN -> false
        }
    }

    private fun studentIsInTeacherClassroom(
        studentClassroomId: UUID,
        teacherProfile: TeacherProfile
    ): Boolean {
        val teacherClassroomIds = classroomService.listByTeacher(teacherProfile.id)
            .map { it.id }
            .toSet()
        return studentClassroomId in teacherClassroomIds
    }

    private fun isLikedAsCoTeacher(classroomId: UUID, teacherId: UUID): Boolean {
        return coTeacherService.isLinkedToClassroom(classroomId, teacherId)
    }
}
