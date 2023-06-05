package com.anahoret.imagilabsapi.coteachers.storage

import com.anahoret.imagilabsapi.classrooms.domain.TeacherRole
import com.anahoret.imagilabsapi.common.storage.BaseEntity
import jakarta.persistence.*
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
    var teacherId: UUID? = null,

    @Column(name = "co_teacher_status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    var coTeacherStatus: TeacherRole = TeacherRole.CO_TEACHER_PENDING
): BaseEntity()
