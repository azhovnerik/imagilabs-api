package com.anahoret.imagilabsapi.tweets.domain

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.stereotype.Service

interface GetTweetsUseCase {

    fun getAll(userProfile: UserProfile): Tweets
}

@Service
class GetTweetsUseCaseImpl(
    private val tweetService: TweetService,
    private val teacherTweetsStateService: TeacherTweetStateService
) : GetTweetsUseCase {

    override fun getAll(userProfile: UserProfile): Tweets {

        return when(userProfile.userType) {
            UserType.ADMIN -> { Tweets(tweetService.getTweets(), null) }
            UserType.TEACHER -> {
                val tweets = tweetService.getTweets()
                val teacherTweetsState = teacherTweetsStateService.getByTeacherId(userProfile.id)
                    ?: teacherTweetsStateService.create(userProfile.id)

                Tweets(tweets, teacherTweetsState)
            }

            else -> Tweets.empty()
        }
    }
}
