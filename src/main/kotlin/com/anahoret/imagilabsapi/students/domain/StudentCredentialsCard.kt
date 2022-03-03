package com.anahoret.imagilabsapi.students.domain

import com.anahoret.imagilabsapi.students.storage.StudentProfileEntity
import java.util.*

class StudentCredentialsCard(
    val id: UUID,
    val username: String,
    val password: String,
    val classroomAccessCode: String,
    val sharedProjectsCount: Long,
    val draftProjectsCount: Long
) {

    companion object {

        fun fromEntity(
            studentProfileEntity: StudentProfileEntity,
            classroomAccessCode: String,
            sharedProjectsCount: Long,
            draftProjectsCount: Long
        ): StudentCredentialsCard {
            return with(studentProfileEntity) {
                StudentCredentialsCard(
                    id!!,
                    username,
                    password,
                    classroomAccessCode,
                    sharedProjectsCount,
                    draftProjectsCount
                )
            }
        }
    }
}
