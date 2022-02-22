package com.anahoret.imagilabsapi.classrooms.storage

import com.anahoret.imagilabsapi.common.storage.BaseEntity
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "classrooms")
class ClassroomEntity(
    @Column(name = "name")
    var name: String,

    @Column(name = "access_code", unique = true)
    var accessCode: String
) : BaseEntity()
