package com.anahoret.imagilabsapi.tweets.domain

import com.anahoret.imagilabsapi.tweets.storage.TweetEntityRepository
import org.springframework.stereotype.Service

interface TweetService {
    fun getTweets(): List<Tweet>
    fun updateTweets(tweets: List<Tweet>): List<Tweet>
}

@Service
class TweetServiceImpl(
    private val tweetEntityRepository: TweetEntityRepository
) : TweetService {

    override fun getTweets(): List<Tweet> {
        return tweetEntityRepository.findAll()
            .map(Tweet::mapFromEntity)
    }

    override fun updateTweets(tweets: List<Tweet>): List<Tweet> {
        val tweetsMap = tweets.associateBy { it.order }

        return tweetEntityRepository.findAll()
            .onEach { it.tweetUri = tweetsMap[it.tweetOrder]?.uri }
            .let(tweetEntityRepository::saveAll)
            .map(Tweet::mapFromEntity)
    }
}
