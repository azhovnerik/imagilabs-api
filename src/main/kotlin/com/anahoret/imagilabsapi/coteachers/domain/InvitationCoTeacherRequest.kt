package com.anahoret.imagilabsapi.coteachers.domain

class InvitationCoTeacherRequest(
    val teacherEmail: String
) {
    fun normalized(): InvitationCoTeacherRequest {
        return InvitationCoTeacherRequest(teacherEmail.trim())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InvitationCoTeacherRequest

        return teacherEmail == other.teacherEmail
    }

    override fun hashCode(): Int {
        return teacherEmail.hashCode()
    }


}
