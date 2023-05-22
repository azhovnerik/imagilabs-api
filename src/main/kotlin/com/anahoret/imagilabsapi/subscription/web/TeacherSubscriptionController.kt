package com.anahoret.imagilabsapi.subscription.web

import arrow.core.Either
import com.anahoret.imagilabsapi.common.web.EmptySuccessResponseDto
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.mapErrors
import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.subscription.domain.CancelTeacherSubscriptionUseCase
import com.anahoret.imagilabsapi.subscription.domain.SetSubscriptionPeriodRequest
import com.anahoret.imagilabsapi.subscription.domain.SetSubscriptionPeriodUseCase
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class TeacherSubscriptionController(
    private val setSubscriptionPeriodUseCase: SetSubscriptionPeriodUseCase,
    private val cancelTeacherSubscriptionUseCase: CancelTeacherSubscriptionUseCase
) {

    @Secured(UserRole.admin)
    @PutMapping("/api/teachers/{teacherId}/subscription")
    fun setSubscriptionPeriodForTeacher(
        @RequestBody setSubscriptionPeriodRequest: SetSubscriptionPeriodRequest,
        @PathVariable teacherId: UUID
    ): ResponseEntity<ResponseDto<Void>> {
        return when (val result = setSubscriptionPeriodUseCase.set(teacherId, setSubscriptionPeriodRequest)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(EmptySuccessResponseDto)
        }
    }

    @Secured(UserRole.admin)
    @PutMapping("/api/teachers/{teacherId}/subscription/cancel")
    fun cancelTeacherSubscription(
        @PathVariable teacherId: UUID,
    ): ResponseEntity<ResponseDto<Void>> {
        return when (val result = cancelTeacherSubscriptionUseCase.cancel(teacherId)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(EmptySuccessResponseDto)
        }
    }

}
