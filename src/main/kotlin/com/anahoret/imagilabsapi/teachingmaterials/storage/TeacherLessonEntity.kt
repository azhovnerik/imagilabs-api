package com.anahoret.imagilabsapi.teachingmaterials.storage

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.util.*

@Entity
@Table(
    name = "teacher_lessons",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uc_teacher_lessons_teacher_id_lesson_index",
            columnNames = ["teacher_id", "lesson_index"]
        )
    ]
)
class TeacherLessonEntity(
    @Column(name = "teacher_id")
    var teacherId: UUID,

    @Column(name = "lesson_index")
    var index: Int,

    @Column(name = "pro_lesson")
    var proLesson: Boolean = true,

    name: String,
    worksheetUri: String,
    slidesUri: String
) : LessonBaseEntity(
    name = name,
    worksheetUri = worksheetUri,
    slidesUri = slidesUri
)
