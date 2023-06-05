package com.anahoret.imagilabsapi.coteachers.domain

import com.anahoret.imagilabsapi.classrooms.domain.TeacherRole
import com.anahoret.imagilabsapi.coteachers.storage.CoTeacherData
import java.util.*

@Suppress("unused")
class CoTeacher(
    val id: UUID,
    val classroomId: UUID,
    val teacherEmail: String,
    val teacherId: UUID?,
    val coTeacherStatus: TeacherRole,
    val name: String?
) {

    companion object {

        fun mapFromCoTeacherData(data: CoTeacherData): CoTeacher {
            return CoTeacher(
                data.id,
                data.classroomId,
                data.teacherEmail,
                data.teacherId,
                data.coTeacherStatus,
                "${data.firstName?:""} ${data.lastName?:""}".trim()
            )
        }
    }
}
