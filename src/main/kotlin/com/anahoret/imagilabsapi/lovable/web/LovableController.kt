package com.anahoret.imagilabsapi.lovable.web

import arrow.core.Either
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.common.web.mapErrors
import com.anahoret.imagilabsapi.lovable.domain.ConnectLovableAccountToTeacherUseCase
import com.anahoret.imagilabsapi.lovable.domain.GetLovableAccountForTeacherUseCase
import com.anahoret.imagilabsapi.lovable.domain.LovableAccount
import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/lovable")
class LovableController(
    private val connectLovableAccountToTeacherUseCase: ConnectLovableAccountToTeacherUseCase,
    private val getLovableAccountForTeacherUseCase: GetLovableAccountForTeacherUseCase
) {

    @PostMapping("/teacher/profile")
    @Secured(UserRole.TEACHER)
    fun connectTeacherProfile(@AuthenticationPrincipal teacher: TeacherProfile): ResponseEntity<ResponseDto<LovableAccount>> {
        return when (val result = connectLovableAccountToTeacherUseCase.connect(teacher)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> return ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @GetMapping("/teacher/profile")
    @Secured(UserRole.TEACHER)
    fun getLovableAccount(@AuthenticationPrincipal teacher: TeacherProfile): ResponseEntity<ResponseDto<LovableAccount>> {
        return getLovableAccountForTeacherUseCase.get(teacher)
            ?.let { ResponseEntity.ok(SuccessResponseDto(it)) }
            ?: ResponseEntity.notFound().build()
    }

}
