package com.anahoret.imagilabsapi.students.domain

import com.anahoret.imagilabsapi.students.storage.StudentProfileEntity
import java.util.*

open class StudentCredentials(
    val id: UUID,
    val username: String,
    val password: String
) {

    companion object {

        fun fromEntity(studentProfileEntity: StudentProfileEntity): StudentCredentials {
            return with(studentProfileEntity) {
                StudentCredentials(id!!, username, password)
            }
        }
    }
}

class StudentClassroomCredentials(
    id: UUID,
    username: String,
    val classroomAccessCode: String,
    password: String
) : StudentCredentials(id, username, password) {

    companion object {

        fun fromEntity(
            studentProfileEntity: StudentProfileEntity,
            classroomAccessCode: String
        ): StudentClassroomCredentials {
            return with(studentProfileEntity) {
                StudentClassroomCredentials(id!!, username, classroomAccessCode, password)
            }
        }
    }
}
