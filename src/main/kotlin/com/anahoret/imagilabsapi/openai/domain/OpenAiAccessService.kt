package com.anahoret.imagilabsapi.openai.domain

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.projects.domain.Project
import com.anahoret.imagilabsapi.projects.domain.ProjectAccessService
import org.springframework.stereotype.Service
import java.util.UUID

interface OpenAiAccessService {
    fun canGetAssistanceForProject(userProfile: UserProfile, project: Project): Boolean
    fun hasTipTokens(studentId: UUID): Boolean
}

@Service
class OpenAiAccessServiceImpl(
    private val projectAccessService: ProjectAccessService,
    private val tipTokensService: TipTokensService
) : OpenAiAccessService {

    override fun canGetAssistanceForProject(userProfile: UserProfile, project: Project): Boolean {
        return projectAccessService.isProjectOwner(userProfile, project)
    }

    override fun hasTipTokens(studentId: UUID): Boolean {
        return tipTokensService.hasTipTokens(studentId) ?: false
    }
}
