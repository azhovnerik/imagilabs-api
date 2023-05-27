package com.anahoret.imagilabsapi.teachers.web

import arrow.core.Either
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.common.web.mapErrors
import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.teachers.domain.*
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
    private val teacherDeleteUseCase: TeacherDeleteUseCase
) {

    @Secured(UserRole.teacher, UserRole.teacherEmailNotVerified)
    @GetMapping("/api/teacher/profile/me")
    fun getProfile(@AuthenticationPrincipal teacherProfile: TeacherProfile): SuccessResponseDto<TeacherProfile> {
        return SuccessResponseDto(teacherProfile)
    }

    @Secured(UserRole.admin)
    @GetMapping("/api/teachers")
    fun getTeachers(
        @RequestParam(required = false) searchQuery: String?,
        sort: Sort
    ): ResponseDto<List<TeacherProfileAdminView>> {
        return SuccessResponseDto(teacherProfileService.listAllForAdmin(searchQuery, sort))
    }

    @Secured(UserRole.admin)
    @GetMapping("/api/teachers/{teacherId}")
    fun getTeacherById(
        @PathVariable teacherId: UUID
    ): ResponseEntity<ResponseDto<TeacherProfileAdminView>> {
        return when (val result = teacherGetUseCase.get(teacherId)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @Secured(UserRole.teacher)
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
