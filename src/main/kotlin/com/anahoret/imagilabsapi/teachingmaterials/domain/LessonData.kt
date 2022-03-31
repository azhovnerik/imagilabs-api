package com.anahoret.imagilabsapi.teachingmaterials.domain

data class LessonData(
    val index: Int,
    val name: String,
    val worksheetUri: String,
    val slidesUri: String,
    val locked: Boolean
) {

    companion object {

        fun fromBundleLesson(bundleLesson: BundleLesson): LessonData {
            return with(bundleLesson) {
                LessonData(index, name, worksheetUri, slidesUri, locked)
            }
        }
    }

}
