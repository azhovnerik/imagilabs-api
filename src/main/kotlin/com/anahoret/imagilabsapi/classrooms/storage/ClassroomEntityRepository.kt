package com.anahoret.imagilabsapi.classrooms.storage;

import org.springframework.data.repository.CrudRepository
import java.util.*

interface ClassroomEntityRepository : CrudRepository<ClassroomEntity, UUID> {

    fun findByAccessCode(accessCode: String): ClassroomEntity?
    fun countByTeacherId(teacherId: UUID): Long
}
