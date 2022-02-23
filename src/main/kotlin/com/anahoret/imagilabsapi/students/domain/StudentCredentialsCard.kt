package com.anahoret.imagilabsapi.students.domain

import com.anahoret.imagilabsapi.students.storage.StudentProfileEntity

class StudentCredentialsCard(
    val studentProfile: StudentProfile,
    val username: String,
    val password: String
) {

    companion object {

        fun fromEntity(studentProfileEntity: StudentProfileEntity): StudentCredentialsCard {
            return with(studentProfileEntity) {
                StudentCredentialsCard(
                    StudentProfile.fromEntity(this),
                    username,
                    password
                )
            }
        }
    }
}
