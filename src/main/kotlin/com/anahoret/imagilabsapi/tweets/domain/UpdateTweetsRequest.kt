package com.anahoret.imagilabsapi.tweets.domain

class UpdateTweetsRequest(
    val tweets: List<Tweet>
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateTweetsRequest

        return tweets == other.tweets
    }

    override fun hashCode(): Int {
        return tweets.hashCode()
    }
}
