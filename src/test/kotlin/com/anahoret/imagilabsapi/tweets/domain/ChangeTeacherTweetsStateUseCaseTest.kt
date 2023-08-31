package com.anahoret.imagilabsapi.tweets.domain

import com.anahoret.imagilabsapi.common.testTeacher
import com.anahoret.imagilabsapi.common.testTweet
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Change teacher tweets state use case")
class ChangeTeacherTweetsStateUseCaseTest {

    private val tweetService = mockk<TweetService>()
    private val teacherTweetsStateService = mockk<TeacherTweetStateService>()
    private val changeTeacherTweetsStateUseCase =
        ChangeTeacherTweetsStateUseCaseImpl(tweetService, teacherTweetsStateService)

    @Test
    fun `should change state and return tweets without creation`() {
        val testTeacher = testTeacher()
        val request = ChangeTeacherTweetsStateRequest(true)
        val testTeacherTweetsState = TeacherTweetsState(testTeacher.id, true, true)
        val testTweet = testTweet()

        every { teacherTweetsStateService.hasTeacherState(testTeacher.id) } returns true
        every { tweetService.getTweets() } returns listOf(testTweet)
        every { teacherTweetsStateService.update(testTeacher.id, true) } returns testTeacherTweetsState

        val result = changeTeacherTweetsStateUseCase.change(testTeacher, request)

        assertAll(
            { Assertions.assertEquals(testTeacherTweetsState, result.teacherTweetSate, "Incorrect teacher tweet state.") },
            { Assertions.assertEquals(1, result.tweets.size, "Incorrect amount of tweets.") },
            { Assertions.assertEquals(testTweet.order, result.tweets[0].order, "Incorrect order.") },
            { Assertions.assertEquals(testTweet.uri, result.tweets[0].uri, "Incorrect uri.") }
        )
    }

    @Test
    fun `should change state and return tweets after creation`() {
        val testTeacher = testTeacher()
        val request = ChangeTeacherTweetsStateRequest(true)
        val testTeacherTweetsState = TeacherTweetsState(testTeacher.id, true, true)
        val testTweet = testTweet()

        every { teacherTweetsStateService.hasTeacherState(testTeacher.id) } returns false
        every { teacherTweetsStateService.create(testTeacher.id) } returns testTeacherTweetsState
        every { tweetService.getTweets() } returns listOf(testTweet)
        every { teacherTweetsStateService.update(testTeacher.id, true) } returns testTeacherTweetsState

        val result = changeTeacherTweetsStateUseCase.change(testTeacher, request)

        assertAll(
            { Assertions.assertEquals(testTeacherTweetsState, result.teacherTweetSate, "Incorrect teacher tweet state.") },
            { Assertions.assertEquals(1, result.tweets.size, "Incorrect amount of tweets.") },
            { Assertions.assertEquals(testTweet.order, result.tweets[0].order, "Incorrect order.") },
            { Assertions.assertEquals(testTweet.uri, result.tweets[0].uri, "Incorrect uri.") }
        )
    }
}
