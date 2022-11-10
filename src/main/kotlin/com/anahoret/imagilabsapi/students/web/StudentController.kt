package com.anahoret.imagilabsapi.students.web

import arrow.core.Either
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.common.web.mapErrors
import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.students.domain.*
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class StudentController(
    private val studentDeleteUseCase: StudentDeleteUseCase,
    private val studentUpdateUseCase: StudentUpdateUseCase,
    private val studentGetUseCase: StudentGetUseCase,
    private val studentResetPasswordUseCase: StudentResetPasswordUseCase
) {

    @Secured(UserRole.teacher)
    @DeleteMapping("/api/students/{studentId}")
    fun deleteStudent(
        @PathVariable studentId: UUID,
        @AuthenticationPrincipal teacherProfile: TeacherProfile
    ): ResponseEntity<ResponseDto<Void>> {
        return when (val result = studentDeleteUseCase.delete(teacherProfile, studentId)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok().build()
        }
    }

    @Secured(UserRole.teacher)
    @PutMapping("/api/students/{studentId}")
    fun updateStudent(
        @PathVariable studentId: UUID,
        @RequestBody studentUpdateRequest: StudentUpdateRequest,
        @AuthenticationPrincipal teacherProfile: TeacherProfile
    ): ResponseEntity<ResponseDto<StudentProfile?>> {
        return when (val result = studentUpdateUseCase.update(teacherProfile, studentId, studentUpdateRequest)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @Secured(UserRole.teacher)
    @PatchMapping("/api/students/{studentId}/password")
    fun resetPassword(
        @PathVariable studentId: UUID,
        @AuthenticationPrincipal teacherProfile: TeacherProfile
    ): ResponseEntity<ResponseDto<StudentCredentials?>> {
        return when (val result = studentResetPasswordUseCase.reset(teacherProfile, studentId)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @Secured(UserRole.teacher)
    @GetMapping("/api/students/{studentId}")
    fun getStudent(
        @PathVariable studentId: UUID,
        @AuthenticationPrincipal teacherProfile: TeacherProfile
    ): ResponseEntity<ResponseDto<StudentDetails?>> {
        return when (val result = studentGetUseCase.get(teacherProfile, studentId)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @Secured(UserRole.student)
    @GetMapping("/api/student/profile/me")
    fun getProfile(@AuthenticationPrincipal studentProfile: StudentProfile): SuccessResponseDto<StudentProfile> {
        return SuccessResponseDto(studentProfile)
    }
}
