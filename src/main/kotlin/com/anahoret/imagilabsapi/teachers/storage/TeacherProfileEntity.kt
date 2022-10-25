package com.anahoret.imagilabsapi.teachers.storage

import com.anahoret.imagilabsapi.common.storage.BaseEntity
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Suppress("unused")
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
    var howDidYouHearAboutUs: String,

    @Column(name = "marketing_email_subscribed")
    var marketingEmailSubscribed: Boolean,

    @Column(name = "email_verification_code")
    var emailVerificationCode: String? = null,

    @Column(name = "email_verified")
    var emailVerified: Boolean = false,

    @Column(name = "password_reset_code")
    var passwordResetCode: String? = null
) : BaseEntity()
