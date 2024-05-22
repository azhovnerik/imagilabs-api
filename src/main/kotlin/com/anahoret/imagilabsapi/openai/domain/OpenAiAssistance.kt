package com.anahoret.imagilabsapi.openai.domain

import java.util.UUID

data class OpenAiAssistance(
    val id: UUID,
    val userId: UUID
)
