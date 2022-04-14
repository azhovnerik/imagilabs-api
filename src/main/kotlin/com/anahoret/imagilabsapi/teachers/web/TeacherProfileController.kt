package com.anahoret.imagilabsapi.teachers.web

import arrow.core.Either
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.common.web.mapErrors
import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.teachers.domain.TeacherGetUseCase
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileAdminView
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class TeacherProfileController(
    private val teacherProfileService: TeacherProfileService,
    private val teacherGetUseCase: TeacherGetUseCase
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

}
