package com.anahoret.imagilabsapi.tweets.domain

import org.springframework.stereotype.Service

interface GetTweetsUseCase {

    fun getAll(): List<Tweet>
}

@Service
class GetTweetsUseCaseImpl(
    private val tweetService: TweetService
) : GetTweetsUseCase {

    override fun getAll(): List<Tweet> {
        return tweetService.getTweets()
    }
}
