package com.anahoret.imagilabsapi.tweets.domain

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
