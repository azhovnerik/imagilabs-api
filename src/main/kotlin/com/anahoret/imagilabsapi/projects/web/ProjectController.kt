package com.anahoret.imagilabsapi.projects.web

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.projects.domain.Project
import com.anahoret.imagilabsapi.projects.domain.ProjectCreateUseCase
import com.anahoret.imagilabsapi.security.UserRole
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ProjectController(
    private val projectCreateUseCase: ProjectCreateUseCase
) {

    @Secured(UserRole.teacher, UserRole.student)
    @PostMapping("/api/projects")
    fun createProject(
        @AuthenticationPrincipal userProfile: UserProfile
    ): ResponseDto<Project> {
        val project = projectCreateUseCase.create(userProfile)
        return SuccessResponseDto(project)
    }

}
