package com.anahoret.imagilabsapi.students.domain

import com.anahoret.imagilabsapi.classrooms.domain.ClassroomCreateRequest
import org.springframework.stereotype.Service

interface StudentProfileService {

    fun createStudents(studentCreateRequests: List<ClassroomCreateRequest.StudentCreateRequest>): List<StudentProfile>

}

@Service
class StudentProfileServiceImpl : StudentProfileService {

    override fun createStudents(studentCreateRequests: List<ClassroomCreateRequest.StudentCreateRequest>): List<StudentProfile> {
        TODO("not implemented")
    }
}
