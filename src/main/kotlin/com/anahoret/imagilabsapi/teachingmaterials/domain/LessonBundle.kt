package com.anahoret.imagilabsapi.teachingmaterials.domain

import java.util.*

class LessonBundle(
    val id: UUID,
    val name: String,
    val lessons: List<BundleLesson>
)
