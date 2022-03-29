package com.anahoret.imagilabsapi.teachingmaterials.storage

import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table
import javax.persistence.UniqueConstraint

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

    @Column(name = "locked")
    var locked: Boolean = true,

    name: String,
    worksheetUri: String,
    slidesUri: String
) : LessonData(
    name = name,
    worksheetUri = worksheetUri,
    slidesUri = slidesUri
)
