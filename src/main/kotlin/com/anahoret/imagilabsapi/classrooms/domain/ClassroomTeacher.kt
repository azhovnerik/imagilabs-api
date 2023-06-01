package com.anahoret.imagilabsapi.classrooms.domain

import com.anahoret.imagilabsapi.coteachers.domain.CoTeacher
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import java.util.*

class ClassroomTeacher(
    val id: UUID?,
    val email: String,
    val role: TeacherRole,
    val isAreYou: Boolean,
    val fullName: String? = null,
    val invitationId: UUID?
) {
    companion object {

        fun mapFromProfile(teacherProfile: TeacherProfile, role: TeacherRole, isAreYou: Boolean): ClassroomTeacher {
            return with(teacherProfile) { ClassroomTeacher(id, email, role, isAreYou, fullName, null) }
        }

        fun mapFromCoTeacher(coTeacher: CoTeacher, role: TeacherRole, isAreYou: Boolean): ClassroomTeacher {
            return with(coTeacher) { ClassroomTeacher(teacherId, teacherEmail, role, isAreYou, name, invitationId = id) }
        }
    }
}
