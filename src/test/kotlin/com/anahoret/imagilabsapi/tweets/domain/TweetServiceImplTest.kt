package com.anahoret.imagilabsapi.tweets.domain

import com.anahoret.imagilabsapi.common.testTweet
import com.anahoret.imagilabsapi.tweets.storage.TweetEntity
import com.anahoret.imagilabsapi.tweets.storage.TweetEntityRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Tweet service")
class TweetServiceImplTest {

    private val tweetEntityRepository = mockk<TweetEntityRepository>()
    private val tweetService = TweetServiceImpl(tweetEntityRepository)

    @DisplayName("When get tweets")
    @Nested
    inner class GetTweets {

        @Test
        fun `should return empty list when tweets not found`() {
            every { tweetEntityRepository.findAll() } returns emptyList()
            assertTrue(tweetService.getTweets().isEmpty())
        }

        @Test
        fun `should return tweets`() {
            val tweet = testTweet()
            every { tweetEntityRepository.findAll() } returns listOf(TweetEntity(tweet.order, tweet.uri))
            val result = tweetService.getTweets()
            assertAll(
                { assertEquals(1, result.size, "Incorrect amount of tweets.") },
                { assertEquals(tweet.order, result[0].order, "Incorrect order.") },
                { assertEquals(tweet.uri, result[0].uri, "Incorrect uri.") }
            )
        }
    }

    @DisplayName("When update tweets")
    @Nested
    inner class UpdateTweets {

        @Test
        fun `should update tweets`() {
            val tweets = listOf(testTweet())
            val entities = listOf(TweetEntity(tweets[0].order, tweets[0].uri))
            every { tweetEntityRepository.findAll() } returns entities
            every { tweetEntityRepository.saveAll(entities) } returns entities
            tweetService.updateTweets(tweets)
            verify(exactly = 1) { tweetEntityRepository.saveAll(entities) }
        }

    }
}
