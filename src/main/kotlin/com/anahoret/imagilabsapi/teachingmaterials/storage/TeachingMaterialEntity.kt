package com.anahoret.imagilabsapi.teachingmaterials.storage

import com.anahoret.imagilabsapi.common.storage.BaseEntity
import com.anahoret.imagilabsapi.teachingmaterials.domain.TeachingMaterialCategory
import javax.persistence.*

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
    var isExternalLink: Boolean,

    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    var category: TeachingMaterialCategory
) : BaseEntity()
