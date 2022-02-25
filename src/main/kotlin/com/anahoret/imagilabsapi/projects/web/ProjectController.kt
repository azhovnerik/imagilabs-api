package com.anahoret.imagilabsapi.projects.web

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.common.web.mapErrors
import com.anahoret.imagilabsapi.projects.domain.Project
import com.anahoret.imagilabsapi.projects.domain.ProjectCreateUseCase
import com.anahoret.imagilabsapi.projects.domain.ProjectUpdateRequest
import com.anahoret.imagilabsapi.projects.domain.ProjectUpdateUseCase
import com.anahoret.imagilabsapi.security.UserRole
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class ProjectController(
    private val projectCreateUseCase: ProjectCreateUseCase,
    private val projectUpdateUseCase: ProjectUpdateUseCase
) {

    @Secured(UserRole.teacher, UserRole.student)
    @PostMapping("/api/projects")
    fun createProject(
        @AuthenticationPrincipal userProfile: UserProfile
    ): ResponseDto<Project> {
        val project = projectCreateUseCase.create(userProfile)
        return SuccessResponseDto(project)
    }

    @Secured(UserRole.teacher, UserRole.student)
    @PutMapping("/api/projects/{projectId}")
    fun updateProject(
        @PathVariable projectId: UUID,
        @RequestBody projectUpdateRequest: ProjectUpdateRequest,
        @AuthenticationPrincipal userProfile: UserProfile
    ): ResponseEntity<ResponseDto<Project?>> {
        return when (val result = projectUpdateUseCase.update(userProfile, projectId, projectUpdateRequest)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

}
