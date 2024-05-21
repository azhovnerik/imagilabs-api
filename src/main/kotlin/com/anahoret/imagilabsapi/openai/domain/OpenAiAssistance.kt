package com.anahoret.imagilabsapi.openai.domain

import java.util.UUID

data class OpenAiAssistance(
    val assistanceId: UUID,
    val userId: UUID
)
