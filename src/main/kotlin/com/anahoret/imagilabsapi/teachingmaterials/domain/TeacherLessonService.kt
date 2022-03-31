package com.anahoret.imagilabsapi.teachingmaterials.domain

import com.anahoret.imagilabsapi.teachingmaterials.storage.TeacherLessonEntity
import com.anahoret.imagilabsapi.teachingmaterials.storage.TeacherLessonEntityRepository
import org.springframework.stereotype.Service
import java.util.*
import javax.persistence.EntityManager

interface TeacherLessonService {

    fun listByTeacherId(teacherId: UUID): List<TeacherLesson>
    fun addAllToTeacher(teacherId: UUID, lessons: List<LessonData>)
    fun update(teacherId: UUID, lessons: List<LessonData>)
}

@Service
class TeacherLessonServiceImpl(
    private val teacherLessonEntityRepository: TeacherLessonEntityRepository,
    private val entityManager: EntityManager
) : TeacherLessonService {

    override fun listByTeacherId(teacherId: UUID): List<TeacherLesson> {
        return teacherLessonEntityRepository.findAllByTeacherIdOrderByIndex(teacherId)
            .map(TeacherLesson.Companion::fromEntity)
    }

    override fun addAllToTeacher(teacherId: UUID, lessons: List<LessonData>) {
        addLessonsToTeacher(teacherId, lessons)
    }

    override fun update(teacherId: UUID, lessons: List<LessonData>) {
        teacherLessonEntityRepository.deleteAllByTeacherId(teacherId)
        /* Flush is needed because by default Hibernate runs inserts before deletes.
            This causes unique constraint exception on (teacherId, lessonIndex) unique key.
            So we delete old lessons, then flush and then insert new lessons.
         */
        entityManager.flush()
        addLessonsToTeacher(teacherId, lessons)
    }

    private fun addLessonsToTeacher(teacherId: UUID, lessons: List<LessonData>) {
        val lastExistingIndex = teacherLessonEntityRepository.findAllByTeacherIdOrderByIndex(teacherId)
            .lastOrNull()?.index
        val remapFromIndex = if (lastExistingIndex == null) 0 else lastExistingIndex + 1
        val newLessonsWithRemappedIndices = lessons.map { it.copy(index = remapFromIndex + it.index) }
        newLessonsWithRemappedIndices.map {
            TeacherLessonEntity(teacherId, it.index, it.locked, it.name, it.worksheetUri, it.slidesUri)
        }.let(teacherLessonEntityRepository::saveAll)
    }

}
