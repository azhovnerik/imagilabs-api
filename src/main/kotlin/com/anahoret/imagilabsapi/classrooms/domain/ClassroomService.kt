package com.anahoret.imagilabsapi.classrooms.domain

import org.springframework.stereotype.Service

interface ClassroomService {

    fun create(classroomCreateRequest: ClassroomCreateRequest): Classroom

}

@Service
class ClassroomServiceImpl : ClassroomService {

    override fun create(classroomCreateRequest: ClassroomCreateRequest): Classroom {
        TODO("not implemented")
    }
}
