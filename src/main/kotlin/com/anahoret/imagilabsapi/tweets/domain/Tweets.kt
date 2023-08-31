package com.anahoret.imagilabsapi.tweets.domain

@Suppress("unused")
class Tweets(
    val tweets: List<Tweet>,
    val teacherTweetSate: TeacherTweetsState?
) {

    companion object {

        fun empty(): Tweets {
            return Tweets(emptyList(), null)
        }
    }
}
