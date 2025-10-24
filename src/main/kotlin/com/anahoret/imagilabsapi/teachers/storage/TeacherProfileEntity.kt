package com.anahoret.imagilabsapi.teachers.storage

import com.anahoret.imagilabsapi.common.storage.BaseEntity
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionData
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.util.*

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

    @Column(name = "how_did_you_hear_about_us_other")
    var howDidYouHearAboutUsOther: String?,

    @Column(name = "marketing_email_subscribed", nullable = false)
    var marketingEmailSubscribed: Boolean,

    @Column(name = "tip_tokens")
    var tipTokens: Int,

    @Column(name = "tip_tokens_replenished_at")
    var tipTokensReplenishedAt: Long,

    @Column(name = "email_verification_code")
    var emailVerificationCode: String? = null,

    @Column(name = "email_verified")
    var emailVerified: Boolean = false,

    @Column(name = "password_reset_code")
    var passwordResetCode: String? = null,

    @Column(name = "subscription_start")
    override var subscriptionStart: Long? = null,

    @Column(name = "subscription_end")
    override var subscriptionEnd: Long? = null,

    @Column(name = "subscription_canceled", nullable = false)
    override var subscriptionCanceled: Boolean = false,

    @Column(name = "ai_chat_onboarding_completed", nullable = false)
    var aiChatOnboardingCompleted: Boolean = false,

    @Column(name = "ai_chat_intro_seen")
    var aiChatIntroSeen: Boolean = false,

    @Column(name = "ed_link_integration_id")
    var edLinkIntegrationId: UUID? = null,

    @Column(name = "ed_link_person_id")
    var edLinkPersonId: UUID? = null
) : BaseEntity(), TeacherSubscriptionData
