package com.anahoret.imagilabsapi.openai.domain

import com.anahoret.imagilabsapi.users.UserType
import java.util.*

data class OpenAiAssistance(
    val id: UUID,
    val userId: UUID,
    val userType: UserType
)
