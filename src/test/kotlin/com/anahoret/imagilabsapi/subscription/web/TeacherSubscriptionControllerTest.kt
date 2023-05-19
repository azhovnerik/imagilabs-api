package com.anahoret.imagilabsapi.subscription.web

import arrow.core.right
import com.anahoret.imagilabsapi.auth.web.jwt.JwtTokenUtil
import com.anahoret.imagilabsapi.common.ControllerTest
import com.anahoret.imagilabsapi.subscription.domain.SetSubscriptionPeriodRequest
import com.anahoret.imagilabsapi.subscription.domain.SetSubscriptionPeriodUseCase
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.*

@DisplayName("Teacher subscription controller")
class TeacherSubscriptionControllerTest {

    private val teacherId = UUID.randomUUID()

    @ExtendWith(SpringExtension::class)
    @DisplayName("when setting subscription period")
    @Nested
    @WebMvcTest(
        TeacherSubscriptionController::class,
        AuthenticationEntryPoint::class,
        JwtTokenUtil::class
    )
    inner class SetSubscriptionPeriodTest : ControllerTest() {

        @MockkBean
        lateinit var setSubscriptionPeriodUseCase: SetSubscriptionPeriodUseCase

        private val request = SetSubscriptionPeriodRequest(100, 200)
        private val payload = JSONObject()
            .put("startDate", 100)
            .put("endDate", 200)
            .toString()

        @Test
        fun `should call set subscription period use case`() {
            every { setSubscriptionPeriodUseCase.set(teacherId, request) } returns Unit.right()

            mvc.perform(
                put("/api/teachers/$teacherId/subscription")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
                    .asAdmin()
            ).andExpect(status().isOk)

            verify { setSubscriptionPeriodUseCase.set(teacherId, request) }
        }

        @Test
        fun `should return 403 Forbidden error when user is teacher`() {
            mvc.perform(
                put("/api/teachers/$teacherId/subscription")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
                    .asTeacher()
            ).andExpect(status().isForbidden)
        }

        @Test
        fun `should return 403 Forbidden error when user is student`() {
            mvc.perform(
                put("/api/teachers/$teacherId/subscription")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
                    .asStudent()
            ).andExpect(status().isForbidden)
        }

    }

}
