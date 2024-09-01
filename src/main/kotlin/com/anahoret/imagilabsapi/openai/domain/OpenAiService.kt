package com.anahoret.imagilabsapi.openai.domain

import com.anahoret.imagilabsapi.openai.domain.OpenAiPrompts.Companion.EX1_ASSISTANT_MESSAGE
import com.anahoret.imagilabsapi.openai.domain.OpenAiPrompts.Companion.EX1_USER_MESSAGE
import com.anahoret.imagilabsapi.openai.domain.OpenAiPrompts.Companion.EX2_ASSISTANT_MESSAGE
import com.anahoret.imagilabsapi.openai.domain.OpenAiPrompts.Companion.EX2_USER_MESSAGE
import com.anahoret.imagilabsapi.openai.domain.OpenAiPrompts.Companion.NEW_PROJECT_DIRECTIVE
import com.anahoret.imagilabsapi.openai.domain.OpenAiPrompts.Companion.SYSTEM_PROMPT
import com.anahoret.imagilabsapi.openai.storage.OpenAiAssistanceContent
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import org.springframework.ai.chat.ChatClient
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.stereotype.Service

interface OpenAiService {
    fun startAssistance(
        userDirective: String,
        chatOptions: OpenAiChatOptions = OpenAiChatOptions()
    ): String

    fun proceedAssistanceOnError(
        input: String,
        content: List<OpenAiAssistanceContent>
    ): String

    fun isAiChatOnboardingCompleted(teacherProfile: TeacherProfile): Boolean
    fun isAiChatOnboardingCompleted(studentProfile: StudentProfile): Boolean
}

@Service
class OpenAiServiceImpl(
    private val chatClient: ChatClient,
    private val studentProfileService: StudentProfileService,
    private val teacherProfileService: TeacherProfileService
) : OpenAiService {

    override fun startAssistance(
        userDirective: String,
        chatOptions: OpenAiChatOptions
    ): String {
        val prompt = Prompt(getInitialMessages(userDirective), chatOptions)
        return chatClient.call(prompt).results[0].output.content
    }

    override fun proceedAssistanceOnError(
        input: String,
        content: List<OpenAiAssistanceContent>
    ): String {
        val initialMessage = getInitialMessages(
            userInput = content.first().userQuestion
        ) + AssistantMessage(content.first().aiResponse)
        val latestMessages = content.drop(1)
            .flatMap { listOf(UserMessage(it.userQuestion), AssistantMessage(it.aiResponse)) }
        val messages = initialMessage + latestMessages + UserMessage(input)
        val prompt = Prompt(messages)
        return chatClient.call(prompt).results[0].output.content
    }

    override fun isAiChatOnboardingCompleted(teacherProfile: TeacherProfile): Boolean {
        return teacherProfileService.isAiChatOnboardingCompleted(teacherProfile.id)
    }

    override fun isAiChatOnboardingCompleted(studentProfile: StudentProfile): Boolean {
        return studentProfileService.isAiChatOnboardingCompleted(studentProfile.id)
    }

    private fun getInitialMessages(userInput: String): List<Message> {
        val systemMessage = SystemMessage(SYSTEM_PROMPT)
        val userMessageExample1 = UserMessage(EX1_USER_MESSAGE)
        val assistantMessageExample1 = AssistantMessage(EX1_ASSISTANT_MESSAGE)
        val userMessageExample2 = UserMessage(EX2_USER_MESSAGE)
        val assistantMessageExample2 = AssistantMessage(EX2_ASSISTANT_MESSAGE)
        val userMessage = UserMessage(NEW_PROJECT_DIRECTIVE + userInput)
        return listOf(systemMessage, userMessageExample1, assistantMessageExample1,
            userMessageExample2, assistantMessageExample2, userMessage)
    }
}
