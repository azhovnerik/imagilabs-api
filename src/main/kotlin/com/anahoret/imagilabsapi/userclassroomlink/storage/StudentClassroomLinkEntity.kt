package com.anahoret.imagilabsapi.userclassroomlink.storage

import org.hibernate.Hibernate
import java.io.Serializable
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "classrooms_students")
@IdClass(StudentClassroomLinkId::class)
class StudentClassroomLinkEntity(
    @Id
    @Column(name = "student_id", nullable = false)
    var studentId: UUID,

    @Id
    @Column(name = "classroom_id", nullable = false)
    var classroomId: UUID
)

class StudentClassroomLinkId(
    var studentId: UUID? = null,
    var classroomId: UUID? = null
) : Serializable {

    companion object {

        private const val serialVersionUID = 3887180182994153166L
    }

    override fun hashCode(): Int = Objects.hash(studentId, classroomId)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

        other as StudentClassroomLinkId

        return studentId == other.studentId &&
            classroomId == other.classroomId
    }
}
