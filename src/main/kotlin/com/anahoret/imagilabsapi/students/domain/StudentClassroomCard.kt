package com.anahoret.imagilabsapi.students.domain

import com.anahoret.imagilabsapi.students.storage.StudentProfileEntity
import java.util.*

open class StudentDetails(
    val id: UUID,
    val name: String,
    val username: String,
    val password: String,
    val classroomId: UUID
) {

    companion object {

        fun fromEntity(studentProfileEntity: StudentProfileEntity): StudentDetails {
            return with(studentProfileEntity) {
                StudentDetails(id!!, name, username, password, classroomId)
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
    classroomId: UUID,
    val classroomAccessCode: String,
    val sharedProjectsCount: Long,
    val draftProjectsCount: Long
) : StudentDetails(id, name, username, password, classroomId) {

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
                    classroomId,
                    classroomAccessCode,
                    sharedProjectsCount,
                    draftProjectsCount
                )
            }
        }
    }
}
