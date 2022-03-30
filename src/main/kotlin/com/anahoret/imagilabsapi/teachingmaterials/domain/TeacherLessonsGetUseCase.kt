package com.anahoret.imagilabsapi.teachingmaterials.domain

import org.springframework.stereotype.Service
import java.util.*

interface TeacherLessonsGetUseCase {

    fun get(teacherId: UUID): List<TeacherLesson>
}

@Service
class TeacherLessonsGetUseCaseImpl(
    private val teacherLessonService: TeacherLessonService
) : TeacherLessonsGetUseCase {

    override fun get(teacherId: UUID): List<TeacherLesson> {
        return teacherLessonService.listByTeacherId(teacherId)
    }
}
