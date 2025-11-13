package com.anahoret.imagilabsapi.students.domain

import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.coteachers.domain.CoTeacherService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.userclassroomlink.domain.StudentClassroomLinkService
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
    private val coTeacherService: CoTeacherService,
    private val studentClassroomLinkService: StudentClassroomLinkService
) : StudentAccessService {

    override fun canDelete(teacherProfile: TeacherProfile, studentProfile: StudentProfile): Boolean {
        return studentIsInTeacherClassroom(studentProfile.id, teacherProfile)
                || isLikedAsCoTeacher(studentProfile.id, teacherProfile.id)
    }

    override fun canUpdate(teacherProfile: TeacherProfile, studentProfile: StudentProfile): Boolean {
        return studentIsInTeacherClassroom(studentProfile.id, teacherProfile)
                || isLikedAsCoTeacher(studentProfile.id, teacherProfile.id)
    }

    override fun canGet(userProfile: UserProfile, studentDetails: StudentDetails): Boolean {
        return when (userProfile.userType) {
            UserType.TEACHER -> studentIsInTeacherClassroom(studentDetails.id, userProfile as TeacherProfile)
                    || isLikedAsCoTeacher(studentDetails.id, userProfile.id)

            UserType.STUDENT -> userProfile.id == studentDetails.id
            UserType.ADMIN -> false
        }
    }

    private fun studentIsInTeacherClassroom(
        studentId: UUID,
        teacherProfile: TeacherProfile
    ): Boolean {
        val teacherClassroomIds = classroomService.listByTeacher(teacherProfile.id)
            .map { it.id }
            .toSet()
        val studentClassroomIds = studentClassroomLinkService.listClassroomIdsByStudent(studentId).toSet()
        return teacherClassroomIds.intersect(studentClassroomIds).isNotEmpty()
    }

    private fun isLikedAsCoTeacher(studentId: UUID, teacherId: UUID): Boolean {
        val studentClassroomIds = studentClassroomLinkService.listClassroomIdsByStudent(studentId).toSet()
        return studentClassroomIds.any { classroomId -> coTeacherService.isLinkedToClassroom(classroomId, teacherId) }
    }
}
