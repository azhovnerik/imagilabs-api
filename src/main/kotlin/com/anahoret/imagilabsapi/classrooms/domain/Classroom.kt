package com.anahoret.imagilabsapi.classrooms.domain

import com.anahoret.imagilabsapi.classrooms.storage.ClassroomEntity
import java.util.*

@Suppress("unused")
class Classroom(
    val id: UUID,
    val name: String,
    val accessCode: String,
    val studentsCount: Long,
    val projectsCount: Long,
    val teacherId: UUID,
    var teacherRole: TeacherRole? = TeacherRole.OWNER
) {

    companion object {

        fun fromEntity(classroomEntity: ClassroomEntity, studentsCount: Long, projectsCount: Long): Classroom {
            return with(classroomEntity) {
                Classroom(id!!, name, accessCode, studentsCount, projectsCount, teacherId)
            }
        }
    }
}

@Suppress("unused")
enum class TeacherRole {
    CO_TEACHER, OWNER
}
