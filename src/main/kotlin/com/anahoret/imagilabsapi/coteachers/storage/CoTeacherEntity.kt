package com.anahoret.imagilabsapi.coteachers.storage

import com.anahoret.imagilabsapi.common.storage.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.util.*

@Entity
@Suppress("unused")
@Table(name = "co_teachers")
class CoTeacherEntity(
    @Column(name = "classroom_id", nullable = false)
    var classroomId: UUID,

    @Column(name = "teacher_email", nullable = false)
    var teacherEmail: String,

    @Column(name = "teacher_id")
    var teacherId: UUID? = null
): BaseEntity()
