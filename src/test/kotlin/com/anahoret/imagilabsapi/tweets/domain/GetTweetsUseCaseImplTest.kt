package com.anahoret.imagilabsapi.tweets.domain

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
    private val getTweetsUseCase = GetTweetsUseCaseImpl(tweetService)

    @Test
    fun `should return tweets`() {
        val tweet = testTweet()
        every { tweetService.getTweets() } returns listOf(tweet)
        val result = getTweetsUseCase.getAll()
        assertAll(
            { assertEquals(1, result.size, "Incorrect amount of tweets.") },
            { assertEquals(tweet.order, result[0].order, "Incorrect order.") },
            { assertEquals(tweet.uri, result[0].uri, "Incorrect uri.") }
        )
    }
}
