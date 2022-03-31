package com.anahoret.imagilabsapi.teachingmaterials.domain

class LessonBundleDataRequest(
    val name: String,
    val defaultBundle: Boolean,
    val lessons: List<LessonData>
)
