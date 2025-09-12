package com.anahoret.imagilabsapi.tweets.web

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.common.web.mapErrors
import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.tweets.domain.*
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class TweetsController(
    private val getTweetsUseCase: GetTweetsUseCase,
    private val updateTweetsUseCase: UpdateTweetsUseCase,
    private val changeTeacherTweetsSateUseCase: ChangeTeacherTweetsStateUseCase
) {

    companion object {

        const val TWEETS_PATH = "/api/tweets"
        const val TWEETS_TEACHER_STATE_PATH = "/api/tweets/teacher/change-state"
    }

    @Secured(UserRole.ADMIN, UserRole.TEACHER)
    @GetMapping(TWEETS_PATH)
    fun getAllTweets(
        @AuthenticationPrincipal userProfile: UserProfile
    ): ResponseEntity<ResponseDto<Tweets>> {
        return when (val result = getTweetsUseCase.getAll(userProfile)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @Secured(UserRole.ADMIN)
    @PutMapping(TWEETS_PATH)
    fun updateTweets(
        @RequestBody request: UpdateTweetsRequest
    ): ResponseEntity<ResponseDto<List<Tweet>>> {
        val tweets = updateTweetsUseCase.update(request)
        return ResponseEntity.ok(SuccessResponseDto(tweets))
    }

    @Secured(UserRole.TEACHER)
    @PutMapping(TWEETS_TEACHER_STATE_PATH)
    fun changeTeacherTweetsState(
        @AuthenticationPrincipal teacherProfile: TeacherProfile,
        @RequestBody request: ChangeTeacherTweetsStateRequest
    ): ResponseEntity<ResponseDto<Tweets>> {
        val tweets = changeTeacherTweetsSateUseCase.change(teacherProfile, request)
        return ResponseEntity.ok(SuccessResponseDto(tweets))
    }
}
