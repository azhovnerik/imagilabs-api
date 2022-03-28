package com.anahoret.imagilabsapi.students.domain

import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.userclassroomlink.domain.StudentClassroomLinkService
import org.springframework.stereotype.Service

interface StudentAccessService {

    fun canDelete(teacherProfile: TeacherProfile, studentProfile: StudentProfile): Boolean
}

@Service
class StudentAccessServiceImpl(
    private val studentClassroomLinkService: StudentClassroomLinkService,
    private val classroomService: ClassroomService
) : StudentAccessService {

    override fun canDelete(teacherProfile: TeacherProfile, studentProfile: StudentProfile): Boolean {
        val studentClassroomIds = studentClassroomLinkService.getLinks(studentProfile.id)
            .map { it.classroomId }
        val teacherClassroomIds = classroomService.listByTeacher(teacherProfile.id)
            .map { it.id }
        return studentClassroomIds.intersect(teacherClassroomIds).isNotEmpty()
    }
}
