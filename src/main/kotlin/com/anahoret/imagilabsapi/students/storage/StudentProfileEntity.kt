package com.anahoret.imagilabsapi.students.storage

import com.anahoret.imagilabsapi.common.storage.BaseEntity
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "student_profiles")
class StudentProfileEntity(
    @Column(name = "name")
    var name: String,

    @Column(name = "username")
    var username: String,

    @Column(name = "password")
    var password: String,

    @Column(name = "classroom_id")
    var classroomId: UUID
) : BaseEntity()
