package com.anahoret.imagilabsapi.teachingmaterials.domain

import com.anahoret.imagilabsapi.teachingmaterials.storage.LessonBundleEntity
import java.util.*

@Suppress("unused")
open class LessonBundleBase(
    val id: UUID,
    val name: String,
    val defaultBundle: Boolean
) {

    companion object {

        fun fromEntity(lessonBundleEntity: LessonBundleEntity): LessonBundleBase {
            return with(lessonBundleEntity) {
                LessonBundleBase(id!!, name, defaultBundle)
            }
        }
    }
}

class LessonBundle(
    id: UUID,
    name: String,
    defaultBundle: Boolean,
    val lessons: List<BundleLesson>
) : LessonBundleBase(id, name, defaultBundle) {

    companion object {

        fun fromEntity(lessonBundleEntity: LessonBundleEntity, lessons: List<BundleLesson>): LessonBundle {
            return with(lessonBundleEntity) {
                LessonBundle(id!!, name, defaultBundle, lessons)
            }
        }
    }
}
