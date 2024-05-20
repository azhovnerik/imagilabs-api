package com.anahoret.imagilabsapi.openai.domain

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.projects.domain.Project
import com.anahoret.imagilabsapi.projects.domain.ProjectAccessService
import org.springframework.stereotype.Service

interface OpenAiAccessService {
    fun canGetAssistance(userProfile: UserProfile, project: Project): Boolean
}

@Service
class OpenAiAccessServiceImpl(
    private val projectAccessService: ProjectAccessService
) : OpenAiAccessService {

    override fun canGetAssistance(userProfile: UserProfile, project: Project): Boolean {
        return projectAccessService.isProjectOwner(userProfile, project)
    }
}
