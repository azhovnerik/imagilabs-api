package com.anahoret.imagilabsapi.students.domain

import com.anahoret.imagilabsapi.students.storage.StudentProfileEntity
import java.util.*

@Suppress("unused")
open class StudentDetails(
    val id: UUID,
    val name: String,
    val username: String,
    val password: String
) {

    companion object {

        fun fromEntity(studentProfileEntity: StudentProfileEntity): StudentDetails {
            return with(studentProfileEntity) {
                StudentDetails(
                    id!!,
                    name,
                    username,
                    password
                )
            }
        }
    }
}

@Suppress("unused")
class StudentClassroomCard(
    id: UUID,
    name: String,
    username: String,
    password: String,
    val classroomAccessCode: String,
    val sharedProjectsCount: Long,
    val draftProjectsCount: Long
) : StudentDetails(id, name, username, password) {

    companion object {

        fun fromEntity(
            studentProfileEntity: StudentProfileEntity,
            classroomAccessCode: String,
            sharedProjectsCount: Long,
            draftProjectsCount: Long
        ): StudentClassroomCard {
            return with(studentProfileEntity) {
                StudentClassroomCard(
                    id!!,
                    name,
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
