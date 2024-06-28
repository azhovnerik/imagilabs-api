package com.anahoret.imagilabsapi.debuggingbuddy.service

import com.anahoret.imagilabsapi.openai.domain.EnvironmentPermissionServiceProduction
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Environment permission service production")
class EnvironmentPermissionServiceProductionTest {

    private val environmentPermissionServiceProduction = EnvironmentPermissionServiceProduction()

    @Test
    fun `should return true when teacher email is in Anadea domain`() {
        val teacherProfile = mockk<TeacherProfile> { every { email } returns "teacher@anadeainc.com" }
        assertTrue(environmentPermissionServiceProduction.canGetAssistanceForProject(teacherProfile))
    }

    @Test
    fun `should return true when teacher email is in ImagiLabs domain`() {
        val teacherProfile = mockk<TeacherProfile> { every { email } returns "teacher@imagilabs.com" }
        assertTrue(environmentPermissionServiceProduction.canGetAssistanceForProject(teacherProfile))
    }

    @Test
    fun `should return false when teacher email is in other domain`() {
        val teacherProfile = mockk<TeacherProfile> { every { email } returns "teacher@example.com" }
        assertFalse(environmentPermissionServiceProduction.canGetAssistanceForProject(teacherProfile))
    }

}
