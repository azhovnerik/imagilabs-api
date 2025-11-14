package com.anahoret.imagilabsapi.lovable.web

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.web.*
import com.anahoret.imagilabsapi.lovable.domain.*
import com.anahoret.imagilabsapi.lovable.domain.accountcards.StudentLovableAccountCardsGenerator
import com.anahoret.imagilabsapi.lovable.domain.accountcards.StudentsLovableAccountCardsFormat
import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.http.MediaType
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
    private val getLovableAccountForStudentUseCase: GetLovableAccountForStudentUseCase,
    private val reconnectLovableAccountForStudentUseCase: ReconnectLovableAccountForStudentUseCase,
    private val enableLovableIntegrationForClassroomUseCase: EnableLovableIntegrationForClassroomUseCase,
    private val setPausedLovableIntegrationForClassroomUseCase: SetPausedLovableIntegrationForClassroomUseCase,
    private val getLovableCredentialsForClassroomUseCase: GetLovableCredentialsForClassroomUseCase,
    private val getLovableIntegrationForClassroomUseCase: GetLovableIntegrationForClassroomUseCase,
    private val studentLovableAccountCardsGenerator: StudentLovableAccountCardsGenerator
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
    fun getLovableAccount(
        @AuthenticationPrincipal user: UserProfile,
        @RequestParam(required = false) classroomId: UUID?
    ): ResponseEntity<ResponseDto<LovableAccount>> {
        return getLovableAccountForUserUseCase.get(user, classroomId)
            ?.let { ResponseEntity.ok(SuccessResponseDto(it)) }
            ?: ResponseEntity.notFound().build()
    }

    @GetMapping("/student/{studentId}")
    @Secured(UserRole.TEACHER)
    fun getLovableAccountForStudent(
        @AuthenticationPrincipal teacher: TeacherProfile,
        @PathVariable studentId: UUID
    ): ResponseEntity<ResponseDto<LovableAccount>> {
        return when (val result = getLovableAccountForStudentUseCase.get(teacher, studentId)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @PostMapping("/classroom/{classroomId}/student/{studentId}")
    @Secured(UserRole.TEACHER)
    fun reconnectLovableAccountForStudent(
        @AuthenticationPrincipal teacher: TeacherProfile,
        @PathVariable studentId: UUID,
        @PathVariable classroomId: UUID
    ): ResponseEntity<ResponseDto<LovableAccount>> {
        return when (val result = reconnectLovableAccountForStudentUseCase.reconnect(teacher, studentId, classroomId)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @PostMapping("/classroom/{classroomId}")
    @Secured(UserRole.TEACHER)
    fun enableIntegrationForClassroom(
        @AuthenticationPrincipal teacher: TeacherProfile,
        @PathVariable classroomId: UUID
    ): ResponseEntity<ResponseDto<LovableClassroom>> {
        return when (val result = enableLovableIntegrationForClassroomUseCase.enable(teacher, classroomId)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @GetMapping("/classroom/{classroomId}")
    @Secured(UserRole.TEACHER, UserRole.STUDENT)
    fun getIntegrationForClassroom(
        @AuthenticationPrincipal user: UserProfile,
        @PathVariable classroomId: UUID
    ): ResponseEntity<ResponseDto<LovableClassroom>> {
        return when (val result = getLovableIntegrationForClassroomUseCase.get(user, classroomId)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> return ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @PutMapping("/classroom/{classroomId}/paused")
    @Secured(UserRole.TEACHER)
    fun setPausedIntegrationForClassroom(
        @AuthenticationPrincipal teacher: TeacherProfile,
        @PathVariable classroomId: UUID,
        @RequestBody setPausedRequest: SetPausedRequest
    ): ResponseEntity<ResponseDto<Void>> {
        setPausedLovableIntegrationForClassroomUseCase.setPaused(teacher, classroomId, setPausedRequest.paused)
        return ResponseEntity.ok(EmptySuccessResponseDto)
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

    @PostMapping("/classroom/{classroomId}/students/download")
    @Secured(UserRole.TEACHER)
    fun getStudentsLovableAccountsForClassroom(
        @AuthenticationPrincipal teacher: TeacherProfile,
        @PathVariable classroomId: UUID,
        @RequestBody downloadRequest: DownloadStudentsLovableAccountsRequest
    ): ResponseEntity<*> {
        return when (val result = studentLovableAccountCardsGenerator.generate(
            teacher, classroomId, downloadRequest
        )) {
            is Either.Left -> mapErrors<Void>(result.value)
            is Either.Right -> result.value.inputStream.toFileResponse(
                fileName = result.value.fileName,
                mediaType = result.value.format.toMediaType()
            )
        }
    }

    class SetPausedRequest(val paused: Boolean)

    private fun StudentsLovableAccountCardsFormat.toMediaType(): MediaType {
        return when (this) {
            StudentsLovableAccountCardsFormat.PDF -> MediaType.APPLICATION_PDF
            StudentsLovableAccountCardsFormat.CSV -> MediaType.parseMediaType("text/csv")
        }
    }

}
