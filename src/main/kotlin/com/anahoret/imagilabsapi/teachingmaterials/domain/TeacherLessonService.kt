package com.anahoret.imagilabsapi.teachingmaterials.domain

import com.anahoret.imagilabsapi.teachingmaterials.storage.TeacherLessonEntity
import com.anahoret.imagilabsapi.teachingmaterials.storage.TeacherLessonEntityRepository
import org.springframework.stereotype.Service
import java.util.*

interface TeacherLessonService {

    fun listByTeacherId(teacherId: UUID): List<TeacherLesson>
    fun deleteAllByTeacherId(teacherId: UUID)
    fun addAllToTeacher(teacherId: UUID, lessons: List<LessonData>)
}

@Service
class TeacherLessonServiceImpl(
    private val teacherLessonEntityRepository: TeacherLessonEntityRepository
) : TeacherLessonService {

    override fun listByTeacherId(teacherId: UUID): List<TeacherLesson> {
        return teacherLessonEntityRepository.findAllByTeacherIdOrderByIndex(teacherId)
            .map(TeacherLesson.Companion::fromEntity)
    }

    override fun deleteAllByTeacherId(teacherId: UUID) {
        teacherLessonEntityRepository.deleteAllByTeacherId(teacherId)
    }

    override fun addAllToTeacher(teacherId: UUID, lessons: List<LessonData>) {
        lessons.map {
            TeacherLessonEntity(teacherId, it.index, it.locked, it.name, it.worksheetUri, it.slidesUri)
        }.let(teacherLessonEntityRepository::saveAll)
    }

}
