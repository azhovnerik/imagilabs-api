package com.anahoret.imagilabsapi.teachers.web

import arrow.core.Either
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.common.web.mapErrors
import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.teachers.domain.*
import com.anahoret.imagilabsapi.teachers.storage.TeacherStatistic
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class TeacherProfileController(
    private val teacherProfileService: TeacherProfileService,
    private val teacherGetUseCase: TeacherGetUseCase,
    private val teacherDeleteUseCase: TeacherDeleteUseCase,
    private val teacherGetStatisticUseCase: TeacherGetStatisticUseCase
) {

    @Secured(UserRole.TEACHER, UserRole.TEACHER_EMAIL_NOT_VERIFIED)
    @GetMapping("/api/teacher/profile/me")
    fun getProfile(@AuthenticationPrincipal teacherProfile: TeacherProfile): SuccessResponseDto<TeacherProfile> {
        return SuccessResponseDto(teacherProfile)
    }

    @Secured(UserRole.TEACHER, UserRole.TEACHER_EMAIL_NOT_VERIFIED)
    @PatchMapping("/api/teacher/profile/me")
    fun updateProfile(
        @AuthenticationPrincipal teacherProfile: TeacherProfile,
        @RequestBody teacherProfileUpdateRequest: TeacherProfileUpdateRequest
    ): SuccessResponseDto<TeacherProfile> {
        TODO()
    }

    @Secured(UserRole.TEACHER)
    @GetMapping("/api/teacher/statistic")
    fun getStatistic(
        @AuthenticationPrincipal teacherProfile: TeacherProfile
    ): ResponseEntity<ResponseDto<TeacherStatistic>> {
        return when (val result = teacherGetStatisticUseCase.get(teacherProfile.id)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @Secured(UserRole.ADMIN)
    @GetMapping("/api/teachers")
    fun getTeachers(
        @RequestParam(required = false) searchQuery: String?,
        sort: Sort
    ): ResponseDto<List<TeacherProfileAdminView>> {
        return SuccessResponseDto(teacherProfileService.listAllForAdmin(searchQuery, sort))
    }

    @Secured(UserRole.ADMIN)
    @GetMapping("/api/teachers/{teacherId}")
    fun getTeacherById(
        @PathVariable teacherId: UUID
    ): ResponseEntity<ResponseDto<TeacherProfileAdminView>> {
        return when (val result = teacherGetUseCase.get(teacherId)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @Secured(UserRole.TEACHER)
    @DeleteMapping("/api/teachers/{teacherId}")
    fun deleteTeacher(
        @PathVariable teacherId: UUID,
        @AuthenticationPrincipal teacherProfile: TeacherProfile
    ): ResponseEntity<ResponseDto<Void>> {
        return when (val result = teacherDeleteUseCase.delete(teacherProfile, teacherId)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok().build()
        }
    }
}
