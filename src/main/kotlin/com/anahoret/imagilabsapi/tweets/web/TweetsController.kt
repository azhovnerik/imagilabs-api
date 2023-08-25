package com.anahoret.imagilabsapi.tweets.web

import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.tweets.domain.GetTweetsUseCase
import com.anahoret.imagilabsapi.tweets.domain.Tweet
import com.anahoret.imagilabsapi.tweets.domain.UpdateTweetsRequest
import com.anahoret.imagilabsapi.tweets.domain.UpdateTweetsUseCase
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class TweetsController(
    private val getTweetsUseCase: GetTweetsUseCase,
    private val updateTweetsUseCase: UpdateTweetsUseCase
) {

    companion object {

        const val TWEETS_PATH = "/api/tweets"
    }

    @Secured(UserRole.admin, UserRole.teacher)
    @GetMapping(TWEETS_PATH)
    fun getAllTweets(): ResponseEntity<ResponseDto<List<Tweet>>> {
        val tweets = getTweetsUseCase.getAll()
        return ResponseEntity.ok(SuccessResponseDto(tweets))
    }

    @Secured(UserRole.admin)
    @PatchMapping(TWEETS_PATH)
    fun updateTweets(
        @RequestBody request: UpdateTweetsRequest
    ): ResponseEntity<ResponseDto<List<Tweet>>> {
        val tweets = updateTweetsUseCase.update(request)
        return ResponseEntity.ok(SuccessResponseDto(tweets))
    }

}
