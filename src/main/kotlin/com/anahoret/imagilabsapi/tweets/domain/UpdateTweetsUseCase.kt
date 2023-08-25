package com.anahoret.imagilabsapi.tweets.domain

import org.springframework.stereotype.Service

interface UpdateTweetsUseCase {

    fun update(request: UpdateTweetsRequest): List<Tweet>
}

@Service
class UpdateTweetsUseCaseImpl(
    private val tweetService: TweetService
) : UpdateTweetsUseCase {

    override fun update(request: UpdateTweetsRequest): List<Tweet> {
        return tweetService.updateTweets(request.tweets)
    }
}
