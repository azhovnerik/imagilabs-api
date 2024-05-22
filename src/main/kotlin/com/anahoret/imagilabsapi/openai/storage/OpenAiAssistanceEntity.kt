package com.anahoret.imagilabsapi.openai.storage

import com.anahoret.imagilabsapi.common.storage.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "open_ai_assistance")
class OpenAiAssistanceEntity(
    @Column(name = "session_id")
    var sessionId: UUID,

    @Column(name = "user_id")
    var userId: UUID,

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
