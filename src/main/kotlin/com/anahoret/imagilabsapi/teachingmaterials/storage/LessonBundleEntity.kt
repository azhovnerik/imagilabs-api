package com.anahoret.imagilabsapi.teachingmaterials.storage

import com.anahoret.imagilabsapi.common.storage.BaseEntity
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "lesson_bundles")
class LessonBundleEntity(
    @Column(name = "name")
    var name: String,

    @Column(name = "default_bundle")
    var defaultBundle: Boolean = false
) : BaseEntity()
