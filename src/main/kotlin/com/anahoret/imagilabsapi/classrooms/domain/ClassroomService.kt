package com.anahoret.imagilabsapi.classrooms.domain

import org.springframework.stereotype.Service
import java.util.*

interface ClassroomService {

    fun create(classroomCreateRequest: ClassroomCreateRequest): Classroom
    fun countByTeacher(teacherId: UUID): Int

}

@Service
class ClassroomServiceImpl : ClassroomService {

    override fun create(classroomCreateRequest: ClassroomCreateRequest): Classroom {
        TODO("not implemented")
    }

    override fun countByTeacher(teacherId: UUID): Int {
        TODO("not implemented")
    }
}
