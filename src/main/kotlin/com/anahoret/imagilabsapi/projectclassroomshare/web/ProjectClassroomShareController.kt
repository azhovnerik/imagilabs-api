package com.anahoret.imagilabsapi.projectclassroomshare.web

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.web.EmptySuccessResponseDto
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.common.web.mapErrors
import com.anahoret.imagilabsapi.projectclassroomshare.domain.ProjectClassroomShareChangeRequest
import com.anahoret.imagilabsapi.projectclassroomshare.domain.ProjectClassroomShareUseCase
import com.anahoret.imagilabsapi.projectclassroomshare.domain.ProjectClassroomUnshareUseCase
import com.anahoret.imagilabsapi.pythoncompiler.domain.RunCodeResponse
import com.anahoret.imagilabsapi.security.UserRole
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ProjectClassroomShareController(
    private val projectClassroomShareUseCase: ProjectClassroomShareUseCase,
    private val projectClassroomUnshareUseCase: ProjectClassroomUnshareUseCase
) {

    @Secured(UserRole.teacher, UserRole.student)
    @PostMapping("/api/project-classroom-share")
    fun shareProjectInClassroom(
        @RequestBody projectClassroomShareChangeRequest: ProjectClassroomShareChangeRequest,
        @AuthenticationPrincipal userProfile: UserProfile
    ): ResponseEntity<ResponseDto<RunCodeResponse?>> {
        return when (val result = projectClassroomShareUseCase.share(userProfile, projectClassroomShareChangeRequest)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @Secured(UserRole.teacher, UserRole.student)
    @DeleteMapping("/api/project-classroom-share")
    fun unshareProjectInClassroom(
        @RequestBody projectClassroomShareChangeRequest: ProjectClassroomShareChangeRequest,
        @AuthenticationPrincipal userProfile: UserProfile
    ): ResponseEntity<ResponseDto<Void>> {
        return when (val result =
            projectClassroomUnshareUseCase.unshare(userProfile, projectClassroomShareChangeRequest)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(EmptySuccessResponseDto)
        }
    }

}
