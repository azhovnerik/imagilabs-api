package com.anahoret.imagilabsapi.students.domain

import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.userclassroomlink.domain.StudentClassroomLinkService
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.stereotype.Service

interface StudentAccessService {

    fun canDelete(teacherProfile: TeacherProfile, studentProfile: StudentProfile): Boolean
    fun canUpdate(teacherProfile: TeacherProfile, studentProfile: StudentProfile): Boolean
    fun canGet(userProfile: UserProfile, studentProfile: StudentProfile): Boolean
}

@Service
class StudentAccessServiceImpl(
    private val studentClassroomLinkService: StudentClassroomLinkService,
    private val classroomService: ClassroomService
) : StudentAccessService {

    override fun canDelete(teacherProfile: TeacherProfile, studentProfile: StudentProfile): Boolean {
        return studentIsInTeacherClassroom(studentProfile, teacherProfile)
    }

    override fun canUpdate(teacherProfile: TeacherProfile, studentProfile: StudentProfile): Boolean {
        return studentIsInTeacherClassroom(studentProfile, teacherProfile)
    }

    override fun canGet(userProfile: UserProfile, studentProfile: StudentProfile): Boolean {
        return when (userProfile.userType) {
            UserType.TEACHER -> studentIsInTeacherClassroom(studentProfile, userProfile as TeacherProfile)
            UserType.STUDENT -> userProfile.id == studentProfile.id
            UserType.ADMIN -> false
        }
    }

    private fun studentIsInTeacherClassroom(
        studentProfile: StudentProfile,
        teacherProfile: TeacherProfile
    ): Boolean {
        val studentClassroomIds = studentClassroomLinkService.getLinks(studentProfile.id)
            .map { it.classroomId }
        val teacherClassroomIds = classroomService.listByTeacher(teacherProfile.id)
            .map { it.id }
            .toSet()
        return studentClassroomIds.intersect(teacherClassroomIds).isNotEmpty()
    }

}
