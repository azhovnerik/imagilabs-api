package com.anahoret.imagilabsapi.teachingmaterials.domain

import com.anahoret.imagilabsapi.teachingmaterials.storage.LessonBundleEntity
import java.util.*

class LessonBundle(
    val id: UUID,
    val name: String,
    val lessons: List<BundleLesson>
) {

    companion object {

        fun fromEntity(lessonBundleEntity: LessonBundleEntity, lessons: List<BundleLesson>): LessonBundle {
            return with(lessonBundleEntity) {
                LessonBundle(id!!, name, lessons)
            }
        }
    }
}
