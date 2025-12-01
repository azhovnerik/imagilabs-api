package com.anahoret.imagilabsapi.classrooms.storage

import com.anahoret.imagilabsapi.common.storage.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "classrooms")
class ClassroomEntity(
    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "access_code", unique = true)
    var accessCode: String,

    @Column(name = "teacher_id", nullable = false)
    var teacherId: UUID,

    @Column(name = "deleted", nullable = false)
    var deleted: Boolean = false,

    @Column(name = "ed_link_integration_id")
    var edLinkIntegrationId: UUID? = null,

    @Column(name = "ed_link_class_id")
    var edLinkClassId: UUID? = null
) : BaseEntity()
