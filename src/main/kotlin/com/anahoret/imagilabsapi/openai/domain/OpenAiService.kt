package com.anahoret.imagilabsapi.openai.domain

import com.anahoret.imagilabsapi.openai.domain.OpenAiPrompts.Companion.FIRST_DIRECTIVE
import com.anahoret.imagilabsapi.openai.domain.OpenAiPrompts.Companion.LIBRARY
import com.anahoret.imagilabsapi.openai.domain.OpenAiPrompts.Companion.SYSTEM_PROMPT
import com.anahoret.imagilabsapi.openai.domain.OpenAiPrompts.Companion.USER_PROMPT
import org.springframework.ai.chat.ChatClient
import org.springframework.ai.chat.ChatResponse
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.SystemPromptTemplate
import org.springframework.stereotype.Service

interface OpenAiService {
    fun startAssistance(
        userCode: String,
        secondDirective: String
    ): ChatResponse
}

@Service
class OpenAiServiceImpl(
    private val chatClient: ChatClient
) : OpenAiService {

    override fun startAssistance(
        userCode: String,
        secondDirective: String
    ): ChatResponse {
        val prompt = getPrompt(userCode, secondDirective)
        return chatClient.call(prompt)
    }

    private fun getPrompt(userCode: String, secondDirective: String): Prompt {
        val systemMessage = SystemMessage(SYSTEM_PROMPT)
        val userMessage = SystemPromptTemplate(USER_PROMPT)
            .createMessage(
                mapOf<String, Any>(
                    "first_directive" to FIRST_DIRECTIVE,
                    "user_code" to userCode,
                    "LIBRARY" to LIBRARY,
                    "second_directive" to secondDirective
                )
            )
        return Prompt(listOf(systemMessage, userMessage))
    }
}


