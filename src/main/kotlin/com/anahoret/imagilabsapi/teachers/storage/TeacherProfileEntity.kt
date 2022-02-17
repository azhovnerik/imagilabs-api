package com.anahoret.imagilabsapi.teachers.storage

import com.anahoret.imagilabsapi.common.storage.BaseEntity
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "teacher_profiles")
class TeacherProfileEntity(
    @Column(name = "email", nullable = false, unique = true)
    var email: String,

    @Column(name = "password_hash", nullable = false)
    var passwordHash: String,

    @Column(name = "first_name", nullable = false)
    var firstName: String,

    @Column(name = "last_name", nullable = false)
    var lastName: String,

    @Column(name = "country", nullable = false)
    var country: String,

    @Column(name = "organization", nullable = false)
    var organization: String,

    @Column(name = "how_did_you_hear_about_us", nullable = false)
    var howDidYouHearAboutUs: String
) : BaseEntity()
