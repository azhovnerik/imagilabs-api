package com.anahoret.imagilabsapi.teachingmaterials.domain

import com.anahoret.imagilabsapi.teachingmaterials.storage.TeacherLessonEntity
import java.util.*

class TeacherLesson(
    val id: UUID,
    val teacherId: UUID,
    val index: Int,
    val name: String,
    val worksheetUri: String,
    val slidesUri: String,
    val locked: Boolean
) {

    companion object {

        fun fromEntity(teacherLessonEntity: TeacherLessonEntity): TeacherLesson {
            return with(teacherLessonEntity) {
                TeacherLesson(id!!, teacherId, index, name, worksheetUri, slidesUri, locked)
            }
        }
    }

}
