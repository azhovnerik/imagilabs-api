package com.anahoret.imagilabsapi.tweets.domain

import com.anahoret.imagilabsapi.common.testAdmin
import com.anahoret.imagilabsapi.common.testStudent
import com.anahoret.imagilabsapi.common.testTeacher
import com.anahoret.imagilabsapi.common.testTweet
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Get tweets use case")
class GetTweetsUseCaseImplTest {

    private val tweetService = mockk<TweetService>()
    private val teacherTweetsStateService = mockk<TeacherTweetStateService>()
    private val getTweetsUseCase = GetTweetsUseCaseImpl(tweetService, teacherTweetsStateService)

    @Test
    fun `should return access denied error for student`() {
        val testStudent = testStudent()
        val tweet = testTweet()

        every { tweetService.getTweets() } returns listOf(tweet)

        val result = getTweetsUseCase.getAll(testStudent)

        assertTrue(result.isLeft(), "Incorrect result.")
    }

    @Test
    fun `should return tweets without state for admin`() {
        val testAdmin = testAdmin()
        val tweet = testTweet()

        every { tweetService.getTweets() } returns listOf(tweet)

        val result = getTweetsUseCase.getAll(testAdmin)

        assertAll(
            { assertEquals(true, result.isRight(), "Incorrect teacher tweet state.") },
            { assertEquals(null, result.getOrNull()!!.teacherTweetSate, "Incorrect teacher tweet state.") },
            { assertEquals(1, result.getOrNull()!!.tweets.size, "Incorrect amount of tweets.") },
            { assertEquals(tweet.order, result.getOrNull()!!.tweets[0].order, "Incorrect order.") },
            { assertEquals(tweet.uri, result.getOrNull()!!.tweets[0].uri, "Incorrect uri.") }
        )
    }

    @Test
    fun `should return tweets with state for teacher`() {
        val testTeacher = testTeacher()
        val tweet = testTweet()
        val teacherTweetsState = TeacherTweetsState(testTeacher.id, isHidden = false, isSowedFeedbackDialog = false)

        every { tweetService.getTweets() } returns listOf(tweet)
        every { teacherTweetsStateService.getByTeacherId(testTeacher.id) } returns teacherTweetsState

        val result = getTweetsUseCase.getAll(testTeacher)

        assertAll(
            { assertEquals(true, result.isRight(), "Incorrect result.") },
            { assertEquals(teacherTweetsState, result.getOrNull()!!.teacherTweetSate, "Incorrect teacher tweet state.") },
            { assertEquals(1, result.getOrNull()!!.tweets.size, "Incorrect amount of tweets.") },
            { assertEquals(tweet.order, result.getOrNull()!!.tweets[0].order, "Incorrect order.") },
            { assertEquals(tweet.uri, result.getOrNull()!!.tweets[0].uri, "Incorrect uri.") }
        )
    }
}
