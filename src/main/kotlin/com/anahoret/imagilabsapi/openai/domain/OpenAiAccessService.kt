package com.anahoret.imagilabsapi.openai.domain

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.projects.domain.Project
import com.anahoret.imagilabsapi.projects.domain.ProjectAccessService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

interface OpenAiAccessService {
    fun canGetAssistanceForProject(userProfile: UserProfile): Boolean
    fun hasTipTokens(userProfile: UserProfile): Boolean
}

@Service
class OpenAiAccessServiceImpl(
    private val tipTokensService: TipTokensService,
    private val environmentPermissionService: EnvironmentPermissionService
) : OpenAiAccessService {

    override fun canGetAssistanceForProject(userProfile: UserProfile): Boolean {
        return environmentPermissionService.canGetAssistanceForProject(userProfile)
    }

    override fun hasTipTokens(userProfile: UserProfile): Boolean {
        return tipTokensService.hasTipTokens(userProfile) ?: false
    }
}

interface EnvironmentPermissionService {
    fun canGetAssistanceForProject(userProfile: UserProfile): Boolean
}

@Profile("stage", "default")
@Service
class EnvironmentPermissionServiceStaging : EnvironmentPermissionService {
    override fun canGetAssistanceForProject(userProfile: UserProfile): Boolean {
        return true
    }
}

@Profile("prod")
@Service
class EnvironmentPermissionServiceProduction : EnvironmentPermissionService {
    override fun canGetAssistanceForProject(userProfile: UserProfile): Boolean {
        return userProfile is TeacherProfile &&
                (userProfile.email.endsWith("@imagilabs.com") || userProfile.email.endsWith("@anadeainc.com") || userProfile.email in specificEmails)
    }

    companion object {
        private val specificEmails = setOf(
            "turnerw@whitehouseisd.org",
            "kelpowers@gmail.com",
            "brianne@codeyourdreams.org",
            "sfortino@tchs.org",
            "margretasgerdur@gmail.com",
            "zeisj@fcpsk12.net",
            "tess.sandbox@gmail.com",
            "pam@devcon.ph",
            "christopher.combs@evsck12.com",
            "jrandy.macdonald@gmail.com"
        )
    }
}
