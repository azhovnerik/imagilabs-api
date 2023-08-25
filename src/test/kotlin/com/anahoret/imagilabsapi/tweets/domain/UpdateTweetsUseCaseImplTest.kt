package com.anahoret.imagilabsapi.tweets.domain

import com.anahoret.imagilabsapi.common.testTweet
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Update tweets use case")
class UpdateTweetsUseCaseImplTest {

    private val tweetService = mockk<TweetService>()
    private val updateTweetsUseCase = UpdateTweetsUseCaseImpl(tweetService)

    @Test
    fun `should update tweet`() {
        val tweets = listOf(testTweet())
        every { tweetService.updateTweets(tweets) } returns tweets
        val result = updateTweetsUseCase.update(UpdateTweetsRequest(tweets))
        assertAll(
            { assertEquals(1, result.size, "Incorrect amount of tweets.") },
            { assertEquals(tweets[0].order, result[0].order, "Incorrect order.") },
            { assertEquals(tweets[0].uri, result[0].uri, "Incorrect uri.") }
        )
    }
}
