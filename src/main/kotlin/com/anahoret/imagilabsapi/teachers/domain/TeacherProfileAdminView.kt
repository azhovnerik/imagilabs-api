package com.anahoret.imagilabsapi.teachers.domain

import com.anahoret.imagilabsapi.schools.domain.School
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscription
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
    val howDidYouHearAboutUsOther: String?,
    val emailVerified: Boolean,
    val marketingEmailSubscribed: Boolean,
    val createdAt: Long,
    val lastModifiedAt: Long,
    val subscription: TeacherSubscription,
    val state: String? = null,
    val schoolRoles: List<SchoolRole> = emptyList(),
    val grades: List<GradeLevel> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val schools: List<School> = emptyList()
) {

    companion object {

        fun fromEntity(entity: TeacherProfileEntity, subscription: TeacherSubscription, schools: List<School> = emptyList()): TeacherProfileAdminView {
            return with(entity) {
                TeacherProfileAdminView(
                    id!!,
                    email,
                    firstName,
                    lastName,
                    country,
                    organization,
                    howDidYouHearAboutUs,
                    howDidYouHearAboutUsOther,
                    emailVerified,
                    marketingEmailSubscribed,
                    createdAt ?: 0,
                    lastModifiedAt?: 0,
                    subscription,
                    state = state,
                    schoolRoles = TeacherProfile.parseSchoolRoles(schoolRoles),
                    grades = TeacherProfile.parseGrades(grades),
                    subjects = TeacherProfile.parseSubjects(subjects),
                    schools = schools
                )
            }
        }
    }
}
