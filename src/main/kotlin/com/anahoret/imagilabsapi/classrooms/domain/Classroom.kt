package com.anahoret.imagilabsapi.classrooms.domain

import com.anahoret.imagilabsapi.classrooms.storage.ClassroomEntity
import java.util.*

class Classroom(
    val id: UUID,
    val name: String,
    val studentsCount: Long
) {
    companion object {

        fun fromEntity(classroomEntity: ClassroomEntity, studentsCount: Long): Classroom {
            return with(classroomEntity) {
                Classroom(id!!, name, studentsCount)
            }
        }
    }
}
