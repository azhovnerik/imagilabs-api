package com.anahoret.imagilabsapi.projectclassroomshare.storage

import org.hibernate.Hibernate
import java.io.Serializable
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "project_classroom_share")
@IdClass(ProjectClassroomShareId::class)
class ProjectClassroomShareEntity(
    @Id
    @Column(name = "project_id", nullable = false)
    var projectId: UUID,

    @Id
    @Column(name = "classroom_id", nullable = false)
    var classroomId: UUID
)

class ProjectClassroomShareId(
    var projectId: UUID? = null,
    var classroomId: UUID? = null
) : Serializable {

    companion object {

        private const val serialVersionUID: Long = -3017047276627654718L
    }

    override fun hashCode(): Int = Objects.hash(projectId, classroomId)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

        other as ProjectClassroomShareId

        return projectId == other.projectId &&
            classroomId == other.classroomId
    }

}

