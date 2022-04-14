package com.anahoret.imagilabsapi.teachers.domain

import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntity
import java.util.*

@Suppress("unused")
class TeacherProfileAdminView(
    val id: UUID,
    val email: String,
    val firstName: String,
    val lastName: String,
    val country: String,
    val organization: String,
    val howDidYouHearAboutUs: String,
    val emailVerified: Boolean,
) {

    companion object {

        fun fromEntity(entity: TeacherProfileEntity): TeacherProfileAdminView {
            return with(entity) {
                TeacherProfileAdminView(
                    id!!, email, firstName, lastName, country, organization, howDidYouHearAboutUs, emailVerified
                )
            }
        }
    }
}
