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
    val deleted: Boolean,
    val schoolName: String?,
    var teacherRole: TeacherRole = TeacherRole.OWNER,
    @field:JsonIgnore val edLinkIntegrationId: UUID? = null,
    @field:JsonIgnore val edLinkClassId: UUID? = null
) {

    val isEdLinkConnected = edLinkIntegrationId != null && edLinkClassId != null

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
                    deleted,
                    schoolName,
                    edLinkIntegrationId = edLinkIntegrationId,
                    edLinkClassId = edLinkClassId
                )
            }
        }
    }
}

enum class TeacherRole {
    CO_TEACHER_PENDING, CO_TEACHER, OWNER
}

class ClassroomPermissions(val canManageCoTeachers: Boolean)
