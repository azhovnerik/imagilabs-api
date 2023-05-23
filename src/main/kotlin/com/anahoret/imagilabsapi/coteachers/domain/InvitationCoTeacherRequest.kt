package com.anahoret.imagilabsapi.coteachers.domain

class InvitationCoTeacherRequest(
    val teacherEmail: String
) {
    fun normalized(): InvitationCoTeacherRequest {
        return InvitationCoTeacherRequest(teacherEmail.trim())
    }
}
