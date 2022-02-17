package com.anahoret.imagilabsapi.teachers.domain

import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntity
import java.util.*

class TeacherCredentials(
    val id: UUID,
    val email: String,
    val passwordHash: String
) {

    companion object {

        fun fromEntity(entity: TeacherProfileEntity): TeacherCredentials {
            return with(entity) {
                TeacherCredentials(id!!, email, passwordHash)
            }
        }
    }
}
