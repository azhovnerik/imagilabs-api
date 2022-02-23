package com.anahoret.imagilabsapi.studentclassroomlink.domain

import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import org.springframework.stereotype.Service

interface StudentClassroomLinkService {

    fun link(classroom: Classroom, students: List<StudentProfile>)

}

@Service
class StudentClassroomLinkServiceImpl : StudentClassroomLinkService {

    override fun link(classroom: Classroom, students: List<StudentProfile>) {
        TODO("not implemented")
    }
}
