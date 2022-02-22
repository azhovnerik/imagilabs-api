package com.anahoret.imagilabsapi.classrooms.domain

import com.anahoret.imagilabsapi.classrooms.storage.ClassroomEntity
import java.util.*

class Classroom(
    val id: UUID,
    val name: String
) {
    companion object {
        fun fromEntity(classroomEntity: ClassroomEntity): Classroom {
            return with(classroomEntity) {
                Classroom(id!!, name)
            }
        }
    }
}
