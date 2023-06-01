package com.anahoret.imagilabsapi.coteachers.domain

import com.anahoret.imagilabsapi.coteachers.storage.CoTeacherEntity
import java.util.*

@Suppress("unused")
class CoTeacher(
    val id: UUID,
    val classroomId: UUID,
    val teacherEmail: String,
    val teacherId: UUID? = null,
    val name: String? = null
) {

    companion object {

        fun mapFromEntity(entity: CoTeacherEntity): CoTeacher {
            return CoTeacher(
                entity.id!!,
                entity.classroomId,
                entity.teacherEmail,
                entity.teacherId,
                entity.name
            )
        }
    }
}
