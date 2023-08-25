package com.anahoret.imagilabsapi.tweets.domain

import com.anahoret.imagilabsapi.tweets.storage.TweetEntity

@Suppress("unused")
class Tweet(
    val order: Long,
    val uri: String?
) {

    companion object {

        fun mapFromEntity(entity: TweetEntity): Tweet {
            return with(entity) { Tweet(tweetOrder, tweetUri) }
        }
    }
}
