package com.anahoret.imagilabsapi.teachers.domain

import org.springframework.stereotype.Service
import java.util.*

interface TeacherService {

    fun createTeacher(request: TeacherSignupRequest)
    fun getTeacherById(id: UUID): TeacherProfile?
}

@Service
class TeacherServiceImpl : TeacherService {

    override fun createTeacher(request: TeacherSignupRequest) {
        TODO("not implemented")
    }

    override fun getTeacherById(id: UUID): TeacherProfile? {
        TODO("not implemented")
    }

}
