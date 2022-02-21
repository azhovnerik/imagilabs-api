package com.anahoret.imagilabsapi.teachers.domain

import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntity
import java.util.*

class TeacherProfile(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val emailVerified: Boolean
) {

    companion object {

        fun fromEntity(entity: TeacherProfileEntity): TeacherProfile {
            return with(entity) {
                TeacherProfile(id!!, firstName, lastName, emailVerified)
            }
        }
    }
}
