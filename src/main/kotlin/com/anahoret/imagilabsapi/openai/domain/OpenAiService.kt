package com.anahoret.imagilabsapi.openai.domain

import com.anahoret.imagilabsapi.openai.domain.OpenAiPrompts.Companion.LIBRARY
import com.anahoret.imagilabsapi.openai.domain.OpenAiPrompts.Companion.SYSTEM_PROMPT
import com.anahoret.imagilabsapi.openai.domain.OpenAiPrompts.Companion.USER_PROMPT
import com.anahoret.imagilabsapi.openai.storage.OpenAiAssistanceContent
import org.springframework.ai.chat.ChatClient
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.SystemPromptTemplate
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.stereotype.Service

interface OpenAiService {
    fun startAssistance(
        userCode: String,
        secondDirective: String,
        chatOptions: OpenAiChatOptions = OpenAiChatOptions()
    ): String

    fun proceedAssistanceOnError(
        input: String,
        content: List<OpenAiAssistanceContent>
    ): String
}

@Service
class OpenAiServiceImpl(
    private val chatClient: ChatClient
) : OpenAiService {

    override fun startAssistance(
        userCode: String,
        secondDirective: String,
        chatOptions: OpenAiChatOptions
    ): String {
        val prompt = Prompt(getInitialMessages(userCode, secondDirective), chatOptions)
        return chatClient.call(prompt).results[0].output.content
    }

    override fun proceedAssistanceOnError(
        input: String,
        content: List<OpenAiAssistanceContent>
    ): String {
        val initialMessage = getInitialMessages(
            userCode = content.first().userCode,
            secondDirective = content.first().userQuestion
        ) + AssistantMessage(content.first().aiResponse)
        val latestMessages = content.drop(1)
            .flatMap { listOf(UserMessage(it.userQuestion), AssistantMessage(it.aiResponse)) }
        val messages = initialMessage + latestMessages + UserMessage(input)
        return chatClient.call(Prompt(messages)).results[0].output.content
    }

    private fun getInitialMessages(userCode: String, secondDirective: String): List<Message> {
        val systemMessage = SystemMessage(SYSTEM_PROMPT)
        val userMessage = SystemPromptTemplate(USER_PROMPT)
            .createMessage(
                mapOf<String, Any>(
                    "user_code" to userCode,
                    "LIBRARY" to LIBRARY,
                    "second_directive" to secondDirective
                )
            )
        return listOf(systemMessage, userMessage)
    }
}
