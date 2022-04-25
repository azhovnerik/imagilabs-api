package com.anahoret.imagilabsapi.students.domain

import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.classrooms.storage.ClassroomEntity
import com.anahoret.imagilabsapi.students.storage.StudentProfileEntity
import java.util.*

@Suppress("unused")
open class StudentDetails(
    val id: UUID,
    val name: String,
    val username: String,
    val password: String,
    val classroomId: UUID,
    val classroomName: String,
    val classroomAccessCode: String
) {

    companion object {

        fun fromEntity(studentProfileEntity: StudentProfileEntity, classroomEntity: ClassroomEntity): StudentDetails {
            return with(studentProfileEntity) {
                StudentDetails(
                    id!!,
                    name,
                    username,
                    password,
                    classroomId,
                    classroomEntity.name,
                    classroomEntity.accessCode
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
    classroomId: UUID,
    classroomName: String,
    classroomAccessCode: String,
    val sharedProjectsCount: Long,
    val draftProjectsCount: Long
) : StudentDetails(id, name, username, password, classroomId, classroomName, classroomAccessCode) {

    companion object {

        fun fromEntity(
            studentProfileEntity: StudentProfileEntity,
            classroom: Classroom,
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
                    classroom.name,
                    classroom.accessCode,
                    sharedProjectsCount,
                    draftProjectsCount
                )
            }
        }
    }
}
