package com.anahoret.imagilabsapi.tweets.domain

import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.stereotype.Service

interface ChangeTeacherTweetsStateUseCase {

    fun change(teacherProfile: TeacherProfile, request: ChangeTeacherTweetsStateRequest): Tweets
}

@Service
class ChangeTeacherTweetsStateUseCaseImpl(
    private val tweetService: TweetService,
    private val teacherTweetsStateService: TeacherTweetStateService
): ChangeTeacherTweetsStateUseCase {

    override fun change(teacherProfile: TeacherProfile, request: ChangeTeacherTweetsStateRequest): Tweets {

        val tweets = tweetService.getTweets()
        val teacherTweetsState = teacherTweetsStateService.update(teacherProfile.id, request.isHidden)

        return Tweets(tweets, teacherTweetsState)
    }
}
