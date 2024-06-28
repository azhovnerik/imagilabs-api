package com.anahoret.imagilabsapi.openai.storage

import com.anahoret.imagilabsapi.common.storage.BaseEntity
import com.anahoret.imagilabsapi.users.UserType
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "open_ai_assistance")
class OpenAiAssistanceEntity(
    @Column(name = "session_id")
    var sessionId: UUID,

    @Column(name = "user_id")
    var userId: UUID,

    @Column(name = "user_type", nullable = false)
    @Enumerated(EnumType.STRING)
    var userType: UserType,

    @Column(name = "project_id")
    var projectId: UUID,

    @Column(name = "user_question")
    var userQuestion: String,

    @Column(name = "ai_response")
    var aiResponse: String,

    @Column(name = "is_helpful")
    var isHelpful: Boolean = false,

    @Column(name = "user_code")
    var userCode: String
) : BaseEntity()
