package com.anahoret.imagilabsapi.students.storage

import com.anahoret.imagilabsapi.common.storage.BaseEntity
import java.util.*
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "student_profiles")
class StudentProfileEntity(
    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "username", nullable = false)
    var username: String,

    @Column(name = "password", nullable = false)
    var password: String,

    @Column(name = "classroom_id")
    var classroomId: UUID,

    @Column(name = "tip_tokens")
    var tipTokens: Int
) : BaseEntity()
