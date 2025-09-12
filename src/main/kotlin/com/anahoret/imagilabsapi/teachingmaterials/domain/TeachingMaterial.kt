package com.anahoret.imagilabsapi.teachingmaterials.domain

import java.util.*

@Suppress("unused")
class TeachingMaterial(
    val id: UUID,
    val index: Int,
    val name: String,
    val path: String?,
    val isExternalLink: Boolean,
    val category: TeachingMaterialCategory,
    val proMaterial: Boolean
) {

    companion object {

        fun worksheetFromBundleLesson(bundleLesson: BundleLesson, proEnabled: Boolean): TeachingMaterial {
            return fromBundleLesson(bundleLesson, TeachingMaterialCategory.WORKSHEETS, proEnabled)
        }

        fun teachingSlidesFromBundleLesson(bundleLesson: BundleLesson, proEnabled: Boolean): TeachingMaterial {
            return fromBundleLesson(bundleLesson, TeachingMaterialCategory.TEACHING_SLIDES, proEnabled)
        }

        private fun fromBundleLesson(
            bundleLesson: BundleLesson,
            category: TeachingMaterialCategory,
            proEnabled: Boolean
        ): TeachingMaterial {
            val uri = when (category) {
                TeachingMaterialCategory.TEACHING_SLIDES -> {
                    if ((bundleLesson.proLesson && proEnabled) || !bundleLesson.proLesson) bundleLesson.slidesUri
                    else null
                }
                TeachingMaterialCategory.WORKSHEETS -> {
                    if ((bundleLesson.proLesson && proEnabled) || !bundleLesson.proLesson) bundleLesson.worksheetUri
                    else null
                }
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
