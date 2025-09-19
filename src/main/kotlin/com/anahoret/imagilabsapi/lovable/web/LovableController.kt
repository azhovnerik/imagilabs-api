package com.anahoret.imagilabsapi.lovable.web

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.common.web.mapErrors
import com.anahoret.imagilabsapi.lovable.domain.*
import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/lovable")
class LovableController(
    private val connectLovableAccountToUserUseCase: ConnectLovableAccountToUserUseCase,
    private val getLovableAccountForUserUseCase: GetLovableAccountForUserUseCase,
    private val enableLovableIntegrationForClassroomUseCase: EnableLovableIntegrationForClassroomUseCase,
    private val setPausedLovableIntegrationForClassroomUseCase: SetPausedLovableIntegrationForClassroomUseCase,
    private val getLovableCredentialsForClassroomUseCase: GetLovableCredentialsForClassroomUseCase,
    private val getLovableIntegrationForClassroomUseCase: GetLovableIntegrationForClassroomUseCase
) {

    @PostMapping("/teacher/profile")
    @Secured(UserRole.TEACHER)
    fun connectTeacherProfile(@AuthenticationPrincipal teacher: TeacherProfile): ResponseEntity<ResponseDto<LovableAccount>> {
        return when (val result = connectLovableAccountToUserUseCase.connect(teacher)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> return ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @GetMapping("/user/profile")
    @Secured(UserRole.TEACHER, UserRole.STUDENT)
    fun getLovableAccount(@AuthenticationPrincipal user: UserProfile): ResponseEntity<ResponseDto<LovableAccount>> {
        return getLovableAccountForUserUseCase.get(user)
            ?.let { ResponseEntity.ok(SuccessResponseDto(it)) }
            ?: ResponseEntity.notFound().build()
    }

    @PostMapping("/classroom/{classroomId}")
    @Secured(UserRole.TEACHER)
    fun enableIntegrationForClassroom(
        @AuthenticationPrincipal teacher: TeacherProfile,
        @PathVariable classroomId: UUID
    ): ResponseEntity<ResponseDto<Void>> {
        return when (val result = enableLovableIntegrationForClassroomUseCase.enable(teacher, classroomId)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> return ResponseEntity.ok().build()
        }
    }

    @GetMapping("/classroom/{classroomId}")
    @Secured(UserRole.TEACHER, UserRole.STUDENT)
    fun getIntegrationForClassroom(
        @AuthenticationPrincipal user: UserProfile,
        @PathVariable classroomId: UUID
    ): ResponseEntity<ResponseDto<Void>> {
        return when (val result = getLovableIntegrationForClassroomUseCase.get(user, classroomId)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> return ResponseEntity.ok().build()
        }
    }

    @PutMapping("/classroom/{classroomId}/paused")
    @Secured(UserRole.TEACHER)
    fun setPausedIntegrationForClassroom(
        @AuthenticationPrincipal teacher: TeacherProfile,
        @PathVariable classroomId: UUID,
        @RequestBody setPausedRequest: SetPausedRequest
    ): ResponseEntity<Void> {
        setPausedLovableIntegrationForClassroomUseCase.setPaused(teacher, classroomId, setPausedRequest.paused)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/classroom/{classroomId}/students")
    @Secured(UserRole.TEACHER)
    fun getStudentsCredentialsForClassroom(
        @AuthenticationPrincipal teacher: TeacherProfile,
        @PathVariable classroomId: UUID
    ): ResponseEntity<ResponseDto<List<LovableAccount>>> {
        return when (val result = getLovableCredentialsForClassroomUseCase.getCredentials(teacher, classroomId)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> return ResponseEntity.ok().body(SuccessResponseDto(result.value))
        }
    }

    class SetPausedRequest(val paused: Boolean)

}
