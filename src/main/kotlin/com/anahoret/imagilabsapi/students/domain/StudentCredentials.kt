package com.anahoret.imagilabsapi.students.domain

import com.anahoret.imagilabsapi.students.storage.StudentProfileEntity
import java.util.*

class StudentCredentials(
    val id: UUID,
    val username: String,
    val classroomAccessCode: String,
    val password: String
) {

    companion object {

        fun fromEntity(studentProfileEntity: StudentProfileEntity, classroomAccessCode: String): StudentCredentials {
            return with(studentProfileEntity) {
                StudentCredentials(id!!, username, classroomAccessCode, password)
            }
        }
    }
}
