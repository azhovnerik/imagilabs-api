package com.anahoret.imagilabsapi.openai.domain.usecases

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.openai.web.OpenAiController.TipTokensResponse
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.utils.DateUtils.getCurrentTime
import org.springframework.stereotype.Service
import java.time.temporal.ChronoUnit

interface GetTipTokensUseCase {
    fun get(userProfile: UserProfile): Either<OperationError, TipTokensResponse>
}

@Service
class GetTipTokensUseCaseImpl(
    private val studentProfileService: StudentProfileService
) : GetTipTokensUseCase {

    override fun get(userProfile: UserProfile): Either<OperationError, TipTokensResponse> {
        val tipTokens = studentProfileService.getStudentById(userProfile.id)?.tipTokens
            ?: return NotFoundError("STUDENT_NOT_FOUND").left()
        val refreshInMin = minutesUntilNextRun()
        return TipTokensResponse(tipTokens, refreshInMin).right()
    }

    private fun minutesUntilNextRun(): Long {
        val currentTime = getCurrentTime()
        val nextRunTime = currentTime.plusHours(1).truncatedTo(ChronoUnit.HOURS)
        return ChronoUnit.MINUTES.between(currentTime, nextRunTime)
    }
}
