package com.anahoret.imagilabsapi.teachingmaterials.domain

import java.util.*

class TeacherLesson(
    val id: UUID,
    val teacherId: UUID,
    val index: Int,
    val name: String,
    val worksheetUri: String,
    val slidesUri: String
)
