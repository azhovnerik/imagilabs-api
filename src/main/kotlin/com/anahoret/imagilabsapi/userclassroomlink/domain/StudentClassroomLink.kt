package com.anahoret.imagilabsapi.userclassroomlink.domain

import com.anahoret.imagilabsapi.userclassroomlink.storage.StudentClassroomLinkEntity
import java.util.*

class StudentClassroomLink(
    val studentId: UUID,
    val classroomId: UUID
) {

    companion object {

        fun fromEntity(studentClassroomLinkEntity: StudentClassroomLinkEntity): StudentClassroomLink {
            return with(studentClassroomLinkEntity) {
                StudentClassroomLink(studentId, classroomId)
            }
        }
    }
}
