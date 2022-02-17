package com.anahoret.imagilabsapi.teachers.storage;

import org.springframework.data.repository.CrudRepository
import java.util.*

interface TeacherProfileEntityRepository : CrudRepository<TeacherProfileEntity, UUID> {

    fun findByEmail(email: String): TeacherProfileEntity?
}
