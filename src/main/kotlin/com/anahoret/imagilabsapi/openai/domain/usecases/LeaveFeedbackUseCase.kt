package com.anahoret.imagilabsapi.openai.domain.usecases

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.openai.domain.OpenAiAssistanceService
import org.springframework.stereotype.Service
import java.util.*

interface LeaveFeedbackUseCase {
    fun leaveFeedback(assistanceId: UUID, isHelpful: Boolean, userProfile: UserProfile): Either<OperationError, Unit>
}

@Service
class LeaveFeedbackUseCaseImpl(
    private val openAiAssistanceService: OpenAiAssistanceService
) : LeaveFeedbackUseCase {
    override fun leaveFeedback(
        assistanceId: UUID,
        isHelpful: Boolean,
        userProfile: UserProfile
    ): Either<OperationError, Unit> {
        val assistance =
            openAiAssistanceService.getById(assistanceId) ?: return NotFoundError("ASSISTANCE_NOT_FOUND").left()
        if (assistance.userId != userProfile.id) return AccessDeniedError("NO_ACCESS_TO_LEAVE_FEEDBACK").left()
        openAiAssistanceService.leaveFeedback(assistanceId, isHelpful)
        return Unit.right()
    }
}
