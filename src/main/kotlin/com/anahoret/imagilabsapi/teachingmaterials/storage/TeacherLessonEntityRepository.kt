package com.anahoret.imagilabsapi.teachingmaterials.storage

import org.springframework.data.repository.CrudRepository
import java.util.*

interface TeacherLessonEntityRepository : CrudRepository<TeacherLessonEntity, UUID> {

    fun findAllByTeacherIdOrderByIndex(teacherId: UUID): List<TeacherLessonEntity>
    fun deleteAllByTeacherId(teacherId: UUID)
}
