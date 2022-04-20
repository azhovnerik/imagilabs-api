package com.anahoret.imagilabsapi.pythoncompiler.web

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.common.web.mapErrors
import com.anahoret.imagilabsapi.pythoncompiler.domain.CodeRunUseCase
import com.anahoret.imagilabsapi.pythoncompiler.domain.RunCodeRequest
import com.anahoret.imagilabsapi.pythoncompiler.domain.RunCodeResponse
import com.anahoret.imagilabsapi.security.UserRole
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class CodeCompilerController(
    private val codeRunUseCase: CodeRunUseCase
) {

    @Secured(UserRole.teacher, UserRole.student)
    @PutMapping("/api/code-compiler/run")
    fun runCode(
        @RequestBody runCodeRequest: RunCodeRequest,
        @AuthenticationPrincipal userProfile: UserProfile
    ): ResponseEntity<ResponseDto<RunCodeResponse?>> {
        return when (val result = codeRunUseCase.run(userProfile, runCodeRequest)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

}
