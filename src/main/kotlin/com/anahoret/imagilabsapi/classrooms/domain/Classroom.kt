package com.anahoret.imagilabsapi.classrooms.domain

import com.anahoret.imagilabsapi.classrooms.storage.ClassroomEntity
import java.util.*

class Classroom(
    val id: UUID,
    val name: String,
    val accessCode: String,
    val studentsCount: Long,
    val teacherId: UUID
) {
    companion object {

        fun fromEntity(classroomEntity: ClassroomEntity, studentsCount: Long): Classroom {
            return with(classroomEntity) {
                Classroom(id!!, name, accessCode, studentsCount, teacherId)
            }
        }
    }
}
