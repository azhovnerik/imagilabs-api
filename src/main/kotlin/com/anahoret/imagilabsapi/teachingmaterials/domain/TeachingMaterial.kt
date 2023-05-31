package com.anahoret.imagilabsapi.teachingmaterials.domain

import java.util.*

@Suppress("unused", "MemberVisibilityCanBePrivate", "CanBeParameter")
class TeachingMaterial(
    val id: UUID,
    val index: Int,
    val name: String,
    path: String,
    val isExternalLink: Boolean,
    val category: TeachingMaterialCategory,
    val proMaterial: Boolean
) {

    val path = if (proMaterial) null else path

    companion object {

        fun worksheetFromBundleLesson(bundleLesson: BundleLesson): TeachingMaterial {
            return fromBundleLesson(bundleLesson, TeachingMaterialCategory.WORKSHEETS)
        }

        fun teachingSlidesFromBundleLesson(bundleLesson: BundleLesson): TeachingMaterial {
            return fromBundleLesson(bundleLesson, TeachingMaterialCategory.TEACHING_SLIDES)
        }

        private fun fromBundleLesson(
            bundleLesson: BundleLesson,
            category: TeachingMaterialCategory
        ): TeachingMaterial {
            val uri = when (category) {
                TeachingMaterialCategory.TEACHING_SLIDES -> bundleLesson.slidesUri
                TeachingMaterialCategory.WORKSHEETS -> bundleLesson.worksheetUri
            }
            return with(bundleLesson) {
                TeachingMaterial(id, index, name, uri, isExternalLink = true, category, proLesson)
            }
        }

    }
}

enum class TeachingMaterialCategory {
    TEACHING_SLIDES, WORKSHEETS
}
