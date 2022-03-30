package com.anahoret.imagilabsapi.teachingmaterials.domain

import com.anahoret.imagilabsapi.teachingmaterials.storage.BundleLessonEntity
import java.util.*

@Suppress("unused")
class BundleLesson(
    val id: UUID,
    val bundleId: UUID,
    val index: Int,
    val name: String,
    val worksheetUri: String,
    val locked: Boolean
) {

    companion object {

        fun fromEntity(bundleLessonEntity: BundleLessonEntity): BundleLesson {
            return with(bundleLessonEntity) {
                BundleLesson(id!!, bundleId, index, name, worksheetUri, locked)
            }
        }
    }
}
