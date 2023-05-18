package com.anahoret.imagilabsapi.teachingmaterials.storage

import com.anahoret.imagilabsapi.common.storage.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass

@MappedSuperclass
class LessonBaseEntity(
    @Column(name = "name")
    var name: String,

    @Column(name = "worksheet_uri")
    var worksheetUri: String,

    @Column(name = "slides_uri")
    var slidesUri: String
) : BaseEntity()
