package com.anahoret.imagilabsapi.teachingmaterials.storage

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.util.*

@Entity
@Table(
    name = "bundle_lessons",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uc_bundle_lessons_bundle_id_lesson_index",
            columnNames = ["bundle_id", "lesson_index"]
        )
    ]
)
class BundleLessonEntity(
    @Column(name = "bundle_id")
    var bundleId: UUID,

    @Column(name = "lesson_index")
    var index: Int,

    @Column(name = "pro_lesson")
    var proLesson: Boolean,

    name: String,
    worksheetUri: String,
    slidesUri: String
) : LessonBaseEntity(
    name = name,
    worksheetUri = worksheetUri,
    slidesUri = slidesUri
)
