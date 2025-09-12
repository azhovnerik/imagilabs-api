package com.anahoret.imagilabsapi.teachingmaterials.domain

data class LessonData(
    val index: Int,
    val name: String,
    val worksheetUri: String,
    val slidesUri: String,
    val proLesson: Boolean
)
