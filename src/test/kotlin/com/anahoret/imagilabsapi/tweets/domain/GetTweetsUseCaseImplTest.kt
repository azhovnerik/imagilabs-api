package com.anahoret.imagilabsapi.tweets.domain

import com.anahoret.imagilabsapi.common.testAdmin
import com.anahoret.imagilabsapi.common.testTeacher
import com.anahoret.imagilabsapi.common.testTweet
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Get tweets use case")
class GetTweetsUseCaseImplTest {

    private val tweetService = mockk<TweetService>()
    private val teacherTweetsStateService = mockk<TeacherTweetStateService>()
    private val getTweetsUseCase = GetTweetsUseCaseImpl(tweetService, teacherTweetsStateService)

    @Test
    fun `should return tweets without state for admin`() {
        val testAdmin = testAdmin()
        val tweet = testTweet()

        every { tweetService.getTweets() } returns listOf(tweet)

        val result = getTweetsUseCase.getAll(testAdmin)

        assertAll(
            { assertEquals(null, result.teacherTweetSate, "Incorrect teacher tweet state.") },
            { assertEquals(1, result.tweets.size, "Incorrect amount of tweets.") },
            { assertEquals(tweet.order, result.tweets[0].order, "Incorrect order.") },
            { assertEquals(tweet.uri, result.tweets[0].uri, "Incorrect uri.") }
        )
    }

    @Test
    fun `should return tweets with state for teacher`() {
        val testTeacher = testTeacher()
        val tweet = testTweet()
        val teacherTweetsState = TeacherTweetsState(testTeacher.id, false, false)

        every { tweetService.getTweets() } returns listOf(tweet)
        every { teacherTweetsStateService.getByTeacherId(testTeacher.id) } returns teacherTweetsState

        val result = getTweetsUseCase.getAll(testTeacher)

        assertAll(
            { assertEquals(teacherTweetsState, result.teacherTweetSate, "Incorrect teacher tweet state.") },
            { assertEquals(1, result.tweets.size, "Incorrect amount of tweets.") },
            { assertEquals(tweet.order, result.tweets[0].order, "Incorrect order.") },
            { assertEquals(tweet.uri, result.tweets[0].uri, "Incorrect uri.") }
        )
    }

    @Test
    fun `should return tweets with state for teacher after creation`() {
        val testTeacher = testTeacher()
        val tweet = testTweet()
        val teacherTweetsState = TeacherTweetsState(testTeacher.id, false, false)

        every { tweetService.getTweets() } returns listOf(tweet)
        every { teacherTweetsStateService.getByTeacherId(testTeacher.id) } returns null
        every { teacherTweetsStateService.create(testTeacher.id) } returns teacherTweetsState

        val result = getTweetsUseCase.getAll(testTeacher)

        assertAll(
            { assertEquals(teacherTweetsState, result.teacherTweetSate, "Incorrect teacher tweet state.") },
            { assertEquals(1, result.tweets.size, "Incorrect amount of tweets.") },
            { assertEquals(tweet.order, result.tweets[0].order, "Incorrect order.") },
            { assertEquals(tweet.uri, result.tweets[0].uri, "Incorrect uri.") }
        )
    }
}
