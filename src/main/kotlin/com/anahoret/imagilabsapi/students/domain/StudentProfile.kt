package com.anahoret.imagilabsapi.students.domain

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.students.storage.StudentProfileEntity
import com.anahoret.imagilabsapi.users.UserType
import java.util.*

class StudentProfile(
    override val id: UUID,
    val name: String,
    val classroomId: UUID
) : UserProfile {

    override val userType = UserType.STUDENT
    override val fullName = name

    companion object {

        fun fromEntity(studentProfileEntity: StudentProfileEntity): StudentProfile {
            return with(studentProfileEntity) {
                StudentProfile(id!!, name, classroomId)
            }
        }
    }
}
