package com.anahoret.imagilabsapi.openai.domain

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.openai.domain.OpenAiAccessService.Companion.availableToSubscribers
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.ZoneId
import java.time.ZonedDateTime

interface OpenAiAccessService {
    companion object {
        val availableToSubscribers = ZonedDateTime.of(2024, 10, 19, 0, 0, 0, 0, ZoneId.of("UTC-7"))
            .toInstant().toEpochMilli()
    }

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
class EnvironmentPermissionServiceProduction(
    private val teacherProfileService: TeacherProfileService,
    private val clock: Clock
) : EnvironmentPermissionService {

    override fun canGetAssistanceForProject(userProfile: UserProfile): Boolean {
        val now = clock.millis()
        return now < availableToSubscribers || when (userProfile) {
            is TeacherProfile -> true
            is StudentProfile -> teacherProfileService.getTeacherByStudent(userProfile.id)?.hasProSubscription(now)
                ?: false

            else -> false
        }
    }

}
