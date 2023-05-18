package com.anahoret.imagilabsapi.subscription.domain

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import java.util.*

interface SetSubscriptionPeriodUseCase {
    fun set(teacherId: UUID, request: SetSubscriptionPeriodRequest): Either<OperationError, Unit>
}

