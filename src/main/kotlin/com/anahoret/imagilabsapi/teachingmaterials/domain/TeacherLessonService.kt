package com.anahoret.imagilabsapi.teachingmaterials.domain

import com.anahoret.imagilabsapi.teachingmaterials.storage.TeacherLessonEntityRepository
import org.springframework.stereotype.Service
import java.util.*

interface TeacherLessonService {

    fun listByTeacherId(teacherId: UUID): List<TeacherLesson>
}

@Service
class TeacherLessonServiceImpl(
    private val teacherLessonEntityRepository: TeacherLessonEntityRepository
) : TeacherLessonService {

    override fun listByTeacherId(teacherId: UUID): List<TeacherLesson> {
        return teacherLessonEntityRepository.findAllByTeacherIdOrderByIndex(teacherId)
            .map(TeacherLesson.Companion::fromEntity)
    }

}
