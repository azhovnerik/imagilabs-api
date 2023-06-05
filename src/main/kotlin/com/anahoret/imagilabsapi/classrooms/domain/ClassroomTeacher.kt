package com.anahoret.imagilabsapi.classrooms.domain

import com.anahoret.imagilabsapi.coteachers.domain.CoTeacher
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import java.util.*

class ClassroomTeacher(
    val id: UUID?,
    val email: String,
    val role: TeacherRole,
    val isCurrentUser: Boolean,
    val fullName: String? = null,
    val invitationId: UUID?
) {
    companion object {

        fun mapFromProfile(teacherProfile: TeacherProfile, role: TeacherRole, isCurrentUser: Boolean): ClassroomTeacher {
            return with(teacherProfile) { ClassroomTeacher(id, email, role, isCurrentUser, fullName, null) }
        }

        fun mapFromCoTeacher(coTeacher: CoTeacher, isCurrentUser: Boolean): ClassroomTeacher {
            return with(coTeacher) { ClassroomTeacher(teacherId, teacherEmail, coTeacherStatus, isCurrentUser, name, invitationId = id) }
        }
    }
}
