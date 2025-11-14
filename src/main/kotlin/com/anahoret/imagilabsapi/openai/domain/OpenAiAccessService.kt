package com.anahoret.imagilabsapi.openai.domain

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.openai.domain.OpenAiAccessService.Companion.hourOfAIRange
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.*

interface OpenAiAccessService {
    companion object {
        val hourOfAIEventTimeZone = ZoneId.of("UTC-8")
        private val hourOfAIEventStart = ZonedDateTime.of(
            LocalDate.of(2025, 11, 8),
            LocalTime.MIN,
            hourOfAIEventTimeZone
        ).toInstant().toEpochMilli()
        private val hourOfAIEventEnd = ZonedDateTime.of(
            LocalDate.of(2025, 12, 15),
            LocalTime.MAX,
            hourOfAIEventTimeZone
        ).toInstant().toEpochMilli()
        val hourOfAIRange = hourOfAIEventStart..hourOfAIEventEnd
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
class EnvironmentPermissionServiceStaging(
    private val teacherProfileService: TeacherProfileService,
    private val clock: Clock
) : EnvironmentPermissionService {
    override fun canGetAssistanceForProject(userProfile: UserProfile): Boolean {
        val now = clock.millis()
        return now in hourOfAIRange || when (userProfile) {
            is TeacherProfile -> true
            is StudentProfile -> teacherProfileService.listTeachersByStudent(userProfile.id)
                .any { it.hasProSubscription(now) }

            else -> false
        }
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
        return now in hourOfAIRange || when (userProfile) {
            is TeacherProfile -> true
            is StudentProfile -> teacherProfileService.listTeachersByStudent(userProfile.id)
                .any { it.hasProSubscription(now) }

            else -> false
        }
    }

}
