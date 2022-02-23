package com.anahoret.imagilabsapi.classrooms.storage

import com.anahoret.imagilabsapi.common.storage.BaseEntity
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "classrooms")
class ClassroomEntity(
    @Column(name = "name")
    var name: String,

    @Column(name = "access_code", unique = true)
    var accessCode: String,

    @Column(name = "teacher_id")
    var teacherId: UUID
) : BaseEntity()
