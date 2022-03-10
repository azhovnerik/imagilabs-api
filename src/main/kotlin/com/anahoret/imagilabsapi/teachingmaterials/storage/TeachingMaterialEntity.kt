package com.anahoret.imagilabsapi.teachingmaterials.storage

import com.anahoret.imagilabsapi.common.storage.BaseEntity
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "teaching_materials")
open class TeachingMaterialEntity(
    @Column(name = "name")
    var name: String,

    @Column(name = "index", unique = true)
    var index: Int,

    @Column(name = "path")
    var path: String,

    @Column(name = "is_external_link")
    var isExternalLink: Boolean
) : BaseEntity()
