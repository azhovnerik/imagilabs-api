package com.anahoret.imagilabsapi.subscription.web

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.web.EmptySuccessResponseDto
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.mapErrors
import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.subscription.domain.CancelTeacherSubscriptionUseCase
import com.anahoret.imagilabsapi.subscription.domain.CheckTeacherAccessProLessonsUseCase
import com.anahoret.imagilabsapi.subscription.domain.SetSubscriptionPeriodRequest
import com.anahoret.imagilabsapi.subscription.domain.SetSubscriptionPeriodUseCase
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class TeacherSubscriptionController(
    private val setSubscriptionPeriodUseCase: SetSubscriptionPeriodUseCase,
    private val cancelTeacherSubscriptionUseCase: CancelTeacherSubscriptionUseCase,
    private val checkTeacherAccessProLessonsUseCase: CheckTeacherAccessProLessonsUseCase
) {

    @Secured(UserRole.ADMIN)
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

    @Secured(UserRole.ADMIN, UserRole.TEACHER)
    @PutMapping("/api/teachers/{teacherId}/subscription/cancel")
    fun cancelTeacherSubscription(
        @PathVariable teacherId: UUID,
        @AuthenticationPrincipal userProfile: UserProfile
    ): ResponseEntity<ResponseDto<Void>> {
        return when (val result = cancelTeacherSubscriptionUseCase.cancel(teacherId, userProfile)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(EmptySuccessResponseDto)
        }
    }

    @Secured(UserRole.TEACHER)
    @GetMapping("/api/teacher/subscription/access/pro-lessons")
    fun checkTeacherHasAccessToProLessons(
        @AuthenticationPrincipal teacherProfile: TeacherProfile
    ): ResponseEntity<ResponseDto<Void>> {
        return when (val result = checkTeacherAccessProLessonsUseCase.checkAccess(teacherProfile)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(EmptySuccessResponseDto)
        }
    }

}
