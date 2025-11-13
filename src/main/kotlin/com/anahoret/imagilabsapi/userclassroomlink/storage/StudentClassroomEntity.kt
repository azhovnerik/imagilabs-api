package com.anahoret.imagilabsapi.userclassroomlink.storage

import jakarta.persistence.*
import java.io.Serializable
import java.util.*

@Entity
@Table(name = "student_classrooms")
@IdClass(StudentClassroomId::class)
class StudentClassroomEntity(
    @Id
    @Column(name = "student_id", nullable = false)
    val studentId: UUID,

    @Id
    @Column(name = "classroom_id", nullable = false)
    val classroomId: UUID
)

data class StudentClassroomId(
    val studentId: UUID = UUID.randomUUID(),
    val classroomId: UUID = UUID.randomUUID()
) : Serializable
