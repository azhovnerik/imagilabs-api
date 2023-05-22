package com.anahoret.imagilabsapi.subscription.web

import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.auth.web.jwt.JwtTokenUtil
import com.anahoret.imagilabsapi.common.ControllerTest
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.subscription.domain.CancelTeacherSubscriptionUseCase
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

        @MockkBean
        lateinit var cancelTeacherSubscriptionUseCase: CancelTeacherSubscriptionUseCase

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

    @ExtendWith(SpringExtension::class)
    @DisplayName("when canceling teacher subscription by admin")
    @Nested
    @WebMvcTest(
        TeacherSubscriptionController::class,
        AuthenticationEntryPoint::class,
        JwtTokenUtil::class
    )
    inner class CancelTeacherSubscriptionByAdmin: ControllerTest() {

        @MockkBean
        lateinit var setSubscriptionPeriodUseCase: SetSubscriptionPeriodUseCase

        @MockkBean
        lateinit var cancelTeacherSubscriptionUseCase: CancelTeacherSubscriptionUseCase

        @Test
        fun `should call cancel teacher subscription use case`() {
            every { cancelTeacherSubscriptionUseCase.cancel(teacherId) } returns Unit.right()

            mvc.perform(
                put("/api/teachers/$teacherId/subscription/cancel")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asAdmin()
            ).andExpect(status().is2xxSuccessful)

            verify { cancelTeacherSubscriptionUseCase.cancel(teacherId) }
        }

        @Test
        fun `should return teacher not found error`() {
            every { cancelTeacherSubscriptionUseCase.cancel(teacherId) } returns NotFoundError("TEACHER_NOT_FOUND").left()

            mvc.perform(
                put("/api/teachers/$teacherId/subscription/cancel")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asAdmin()
            ).andExpect(status().isNotFound)
        }

        @Test
        fun `should return 403 Forbidden error when user is student`() {
            mvc.perform(
                put("/api/teachers/$teacherId/subscription/cancel")
                    .contentType(MediaType.APPLICATION_JSON)
                    .asStudent()
            ).andExpect(status().isForbidden)
        }
    }

}
