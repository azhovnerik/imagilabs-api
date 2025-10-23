package com.anahoret.imagilabsapi.students.storage

import com.anahoret.imagilabsapi.common.storage.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "student_profiles")
class StudentProfileEntity(
    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "username", nullable = false)
    var username: String,

    @Column(name = "password", nullable = false)
    var password: String,

    @Column(name = "classroom_id")
    var classroomId: UUID,

    @Column(name = "tip_tokens")
    var tipTokens: Int,

    @Column(name = "tip_tokens_replenished_at")
    var tipTokensReplenishedAt: Long,

    @Column(name = "ai_chat_onboarding_completed", nullable = false)
    var aiChatOnboardingCompleted: Boolean = false,

    @Column(name = "ed_link_integration_id")
    var edLinkIntegrationId: UUID? = null,

    @Column(name = "ed_link_person_id")
    var edLinkPersonId: UUID? = null
) : BaseEntity()
