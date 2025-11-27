package com.anahoret.imagilabsapi.classrooms.domain

import com.anahoret.imagilabsapi.classrooms.storage.ClassroomEntity
import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*

@Suppress("unused")
class Classroom(
    val id: UUID,
    val name: String,
    val accessCode: String,
    val studentsCount: Long,
    val projectsCount: Long,
    val teacherId: UUID,
    val teachersCount: Long,
    val blocked: Boolean,
    val permissions: ClassroomPermissions,
    var teacherRole: TeacherRole = TeacherRole.OWNER,
    @field:JsonIgnore val edLinkId: UUID? = null
) {

    companion object {

        fun fromEntity(
            classroomEntity: ClassroomEntity,
            studentsCount: Long,
            projectsCount: Long,
            coTeachersCount: Long,
            blocked: Boolean,
            permissions: ClassroomPermissions
        ): Classroom {

            return with(classroomEntity) {
                Classroom(
                    id!!,
                    name,
                    accessCode,
                    studentsCount,
                    projectsCount,
                    teacherId,
                    coTeachersCount + 1,
                    blocked,
                    permissions,
                    edLinkId = edLinkId
                )
            }
        }
    }
}

enum class TeacherRole {
    CO_TEACHER_PENDING, CO_TEACHER, OWNER
}

class ClassroomPermissions(val canManageCoTeachers: Boolean)
