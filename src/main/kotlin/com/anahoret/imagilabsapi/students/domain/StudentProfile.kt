package com.anahoret.imagilabsapi.students.domain

import com.anahoret.imagilabsapi.students.storage.StudentProfileEntity
import java.util.*

class StudentProfile(
    val id: UUID,
    val name: String
) {
    companion object {
        fun fromEntity(studentProfileEntity: StudentProfileEntity): StudentProfile {
            return with (studentProfileEntity) {
                StudentProfile(id!!, name)
            }
        }
    }
}
