package com.anahoret.imagilabsapi.tweets.web

import com.anahoret.imagilabsapi.auth.web.jwt.JwtTokenUtil
import com.anahoret.imagilabsapi.common.ControllerTest
import com.anahoret.imagilabsapi.common.testAdmin
import com.anahoret.imagilabsapi.common.testTeacher
import com.anahoret.imagilabsapi.tweets.domain.*
import com.anahoret.imagilabsapi.tweets.web.TweetsController.Companion.TWEETS_PATH
import com.anahoret.imagilabsapi.tweets.web.TweetsController.Companion.TWEETS_TEACHER_STATE_PATH
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@DisplayName("Tweets controller")
class TweetsControllerTest {

    @ExtendWith(SpringExtension::class)
    @DisplayName("when get tweets")
    @Nested
    @WebMvcTest(
        TweetsController::class,
        AuthenticationEntryPoint::class,
        JwtTokenUtil::class
    )
    @Suppress("unused")
    inner class GetTweets : ControllerTest() {

        @MockkBean
        lateinit var getTweetsUseCase: GetTweetsUseCase

        @MockkBean
        lateinit var updateTweetsUseCase: UpdateTweetsUseCase

        @MockkBean
        lateinit var changeTeacherTweetsSateUseCase: ChangeTeacherTweetsStateUseCase

        @Test
        fun `should return forbidden when user is student`() {
            mvc.perform(
                MockMvcRequestBuilders.get(TWEETS_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .asStudent()
            ).andExpect(MockMvcResultMatchers.status().isForbidden)
        }

        @Test
        fun `should return success when user is admin`() {
            val testAdmin = testAdmin()

            every { getTweetsUseCase.getAll(testAdmin) } returns Tweets.empty()

            mvc.perform(
                MockMvcRequestBuilders.get(TWEETS_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .asAdmin(testAdmin)
            ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

            verify { getTweetsUseCase.getAll(testAdmin) }
        }

        @Test
        fun `should return success when user is teacher`() {
            val testTeacher = testTeacher()

            every { getTweetsUseCase.getAll(testTeacher) } returns Tweets.empty()

            mvc.perform(
                MockMvcRequestBuilders.get(TWEETS_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .asTeacher(testTeacher)
            ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

            verify { getTweetsUseCase.getAll(testTeacher) }
        }
    }

    @ExtendWith(SpringExtension::class)
    @DisplayName("when update tweets")
    @Nested
    @WebMvcTest(
        TweetsController::class,
        AuthenticationEntryPoint::class,
        JwtTokenUtil::class
    )
    @Suppress("unused")
    inner class UpdateTweets : ControllerTest() {

        @MockkBean
        lateinit var getTweetsUseCase: GetTweetsUseCase

        @MockkBean
        lateinit var updateTweetsUseCase: UpdateTweetsUseCase

        @MockkBean
        lateinit var changeTeacherTweetsSateUseCase: ChangeTeacherTweetsStateUseCase

        private val array = JSONArray()
        private val payload = JSONObject()
            .put("tweets", array)
            .toString()

        @Test
        fun `should return forbidden when user is teacher`() {
            mvc.perform(
                MockMvcRequestBuilders.put(TWEETS_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
                    .asTeacher()
            ).andExpect(MockMvcResultMatchers.status().isForbidden)
        }

        @Test
        fun `should return forbidden when user is student`() {
            mvc.perform(
                MockMvcRequestBuilders.put(TWEETS_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
                    .asStudent()
            ).andExpect(MockMvcResultMatchers.status().isForbidden)
        }

        @Test
        fun `should return success`() {
            val request = UpdateTweetsRequest(emptyList())

            every { updateTweetsUseCase.update(request) } returns emptyList()

            mvc.perform(
                MockMvcRequestBuilders.put(TWEETS_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
                    .asAdmin()
            ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

            verify { updateTweetsUseCase.update(request) }
        }
    }

    @ExtendWith(SpringExtension::class)
    @DisplayName("when get tweets")
    @Nested
    @WebMvcTest(
        TweetsController::class,
        AuthenticationEntryPoint::class,
        JwtTokenUtil::class
    )
    @Suppress("unused")
    inner class ChangeTeacherTweetsState: ControllerTest() {

        @MockkBean
        lateinit var getTweetsUseCase: GetTweetsUseCase

        @MockkBean
        lateinit var updateTweetsUseCase: UpdateTweetsUseCase

        @MockkBean
        lateinit var changeTeacherTweetsSateUseCase: ChangeTeacherTweetsStateUseCase

        private val payload = JSONObject()
            .put("isHidden", true)
            .toString()

        @Test
        fun `should return forbidden when user is admin`() {
            mvc.perform(
                MockMvcRequestBuilders.put(TWEETS_TEACHER_STATE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
                    .asAdmin()
            ).andExpect(MockMvcResultMatchers.status().isForbidden)
        }

        @Test
        fun `should return forbidden when user is student`() {
            mvc.perform(
                MockMvcRequestBuilders.put(TWEETS_TEACHER_STATE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
                    .asStudent()
            ).andExpect(MockMvcResultMatchers.status().isForbidden)
        }

        @Test
        fun `should return success`() {
            val testTeacher = testTeacher()
            val request = ChangeTeacherTweetsStateRequest(true)

            every { changeTeacherTweetsSateUseCase.change(testTeacher, request) } returns Tweets.empty()

            mvc.perform(
                MockMvcRequestBuilders.put(TWEETS_TEACHER_STATE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
                    .asTeacher(testTeacher)
            ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

            verify { changeTeacherTweetsSateUseCase.change(testTeacher, request) }
        }
    }
}
