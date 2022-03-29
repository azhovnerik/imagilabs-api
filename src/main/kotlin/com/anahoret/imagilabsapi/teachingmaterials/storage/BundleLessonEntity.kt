package com.anahoret.imagilabsapi.teachingmaterials.storage

import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table
import javax.persistence.UniqueConstraint

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

    name: String,
    worksheetUri: String,
    slidesUri: String
) : LessonData(
    name = name,
    worksheetUri = worksheetUri,
    slidesUri = slidesUri
)
