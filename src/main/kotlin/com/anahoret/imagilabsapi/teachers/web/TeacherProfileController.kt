package com.anahoret.imagilabsapi.teachers.web

import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TeacherProfileController(
    private val teacherProfileService: TeacherProfileService
) {

    @Secured(UserRole.teacher, UserRole.teacherEmailNotVerified)
    @GetMapping("/api/teacher/profile/me")
    fun getProfile(@AuthenticationPrincipal teacherProfile: TeacherProfile): SuccessResponseDto<TeacherProfile> {
        return SuccessResponseDto(teacherProfile)
    }

    @Secured(UserRole.admin)
    @GetMapping("/api/teachers")
    fun getTeachers(): ResponseDto<List<TeacherProfile>> {
        return SuccessResponseDto(teacherProfileService.listAll())
    }

}
