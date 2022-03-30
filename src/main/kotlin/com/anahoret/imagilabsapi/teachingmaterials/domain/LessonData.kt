package com.anahoret.imagilabsapi.teachingmaterials.domain

class LessonData(
    val index: Int,
    val name: String,
    val worksheetUri: String,
    val slidesUri: String,
    val locked: Boolean
)
