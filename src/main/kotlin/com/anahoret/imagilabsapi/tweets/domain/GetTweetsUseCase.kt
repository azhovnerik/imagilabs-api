package com.anahoret.imagilabsapi.tweets.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.stereotype.Service

interface GetTweetsUseCase {

    fun getAll(userProfile: UserProfile): Either<OperationError, Tweets>
}

@Service
class GetTweetsUseCaseImpl(
    private val tweetService: TweetService,
    private val teacherTweetsStateService: TeacherTweetStateService
) : GetTweetsUseCase {

    override fun getAll(userProfile: UserProfile): Either<OperationError, Tweets> {

        return when(userProfile.userType) {
            UserType.ADMIN -> { Tweets(tweetService.getTweets(), null).right() }
            UserType.TEACHER -> {
                val tweets = tweetService.getTweets()
                val teacherTweetsState = teacherTweetsStateService.getByTeacherId(userProfile.id)

                Tweets(tweets, teacherTweetsState).right()
            }

            else -> AccessDeniedError("ACCESS_TO_TWEETS_DENIED").left()
        }
    }
}
