package com.anahoret.imagilabsapi.teachingmaterials.storage

import com.anahoret.imagilabsapi.common.storage.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.util.UUID

@Entity
@Table(
    name = "teachers_bundles",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uc_teacher_id_bundle_id",
            columnNames = ["teacher_id", "bundle_id"]
        )
    ]
)
class TeacherBundleEntity(

    @Column(name = "teacher_id", nullable = false)
    var teacherId: UUID,

    @Column(name = "bundle_id", nullable = false)
    var bundleId: UUID

): BaseEntity()
