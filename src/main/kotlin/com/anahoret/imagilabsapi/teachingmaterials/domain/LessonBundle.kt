package com.anahoret.imagilabsapi.teachingmaterials.domain

import com.anahoret.imagilabsapi.teachingmaterials.storage.LessonBundleEntity
import java.util.*

open class LessonBundleBase(
    val id: UUID,
    val name: String,
) {

    companion object {

        fun fromEntity(lessonBundleEntity: LessonBundleEntity): LessonBundleBase {
            return with(lessonBundleEntity) {
                LessonBundleBase(id!!, name)
            }
        }
    }
}

class LessonBundle(
    id: UUID,
    name: String,
    val lessons: List<BundleLesson>
) : LessonBundleBase(id, name) {

    companion object {

        fun fromEntity(lessonBundleEntity: LessonBundleEntity, lessons: List<BundleLesson>): LessonBundle {
            return with(lessonBundleEntity) {
                LessonBundle(id!!, name, lessons)
            }
        }
    }
}
