package com.anahoret.imagilabsapi.teachingmaterials.domain

import java.util.*

@Suppress("unused")
class TeachingMaterial(
    val id: UUID,
    val index: Int,
    val name: String,
    val path: String,
    val isExternalLink: Boolean,
    val category: TeachingMaterialCategory
) {

    companion object {

        fun worksheetFromTeacherLesson(teacherLesson: TeacherLesson): TeachingMaterial {
            return fromTeacherLesson(teacherLesson, TeachingMaterialCategory.WORKSHEETS)
        }

        fun teachingSlidesFromTeacherLesson(teacherLesson: TeacherLesson): TeachingMaterial {
            return fromTeacherLesson(teacherLesson, TeachingMaterialCategory.TEACHING_SLIDES)
        }

        private fun fromTeacherLesson(
            teacherLesson: TeacherLesson,
            category: TeachingMaterialCategory
        ): TeachingMaterial {
            val uri = when (category) {
                TeachingMaterialCategory.TEACHING_SLIDES -> teacherLesson.slidesUri
                TeachingMaterialCategory.WORKSHEETS -> teacherLesson.worksheetUri
            }
            return with(teacherLesson) {
                TeachingMaterial(id, index, name, uri, isExternalLink = false, category)
            }
        }
    }
}

enum class TeachingMaterialCategory {
    TEACHING_SLIDES, WORKSHEETS
}
