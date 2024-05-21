package com.anahoret.imagilabsapi.openai.domain

import com.anahoret.imagilabsapi.openai.domain.OpenAiPrompts.Companion.FIRST_DIRECTIVE
import com.anahoret.imagilabsapi.openai.domain.OpenAiPrompts.Companion.LIBRARY
import com.anahoret.imagilabsapi.openai.domain.OpenAiPrompts.Companion.SYSTEM_PROMPT
import com.anahoret.imagilabsapi.openai.domain.OpenAiPrompts.Companion.USER_PROMPT
import com.anahoret.imagilabsapi.openai.web.OpenAiController.AssistanceOnSuccessRequest
import org.springframework.ai.chat.ChatClient
import org.springframework.ai.chat.ChatResponse
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.SystemPromptTemplate
import org.springframework.stereotype.Service

interface OpenAiService {
    fun getAssistanceOnSuccess(
        userRequest: AssistanceOnSuccessRequest,
        secondDirectiveWithQuestion: String
    ): ChatResponse
}

@Service
class OpenAiServiceImpl(
    private val chatClient: ChatClient
) : OpenAiService {

    override fun getAssistanceOnSuccess(
        userRequest: AssistanceOnSuccessRequest,
        secondDirectiveWithQuestion: String
    ): ChatResponse {
        val prompt = getPrompt(userRequest.userCode, secondDirectiveWithQuestion)
        return chatClient.call(prompt)
    }

    private fun getPrompt(userCode: String, secondDirectiveWithQuestion: String): Prompt {
        val systemMessage = SystemMessage(SYSTEM_PROMPT)
        val userMessage = SystemPromptTemplate(USER_PROMPT)
            .createMessage(
                mapOf<String, Any>(
                    "first_directive" to FIRST_DIRECTIVE,
                    "user_code" to userCode,
                    "LIBRARY" to LIBRARY,
                    "second_directive" to secondDirectiveWithQuestion
                )
            )
        return Prompt(listOf(systemMessage, userMessage))
    }
}


