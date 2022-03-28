package com.anahoret.imagilabsapi.projects.web

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.data.UnpagedSorted
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.common.web.mapErrors
import com.anahoret.imagilabsapi.projects.domain.ProjectCard
import com.anahoret.imagilabsapi.projects.domain.ProjectDetails
import com.anahoret.imagilabsapi.projects.domain.ProjectUpdateRequest
import com.anahoret.imagilabsapi.projects.domain.usecases.*
import com.anahoret.imagilabsapi.pythoncompiler.RunCodeResponse
import com.anahoret.imagilabsapi.security.UserRole
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class ProjectController(
    private val projectCreateUseCase: ProjectCreateUseCase,
    private val projectUpdateUseCase: ProjectUpdateUseCase,
    private val projectRunUseCase: ProjectRunUseCase,
    private val projectGetUseCase: ProjectGetUseCase,
    private val projectListUseCase: ProjectListUseCase,
    private val projectDeleteUseCase: ProjectDeleteUseCase,
) {

    @Secured(UserRole.teacher, UserRole.student)
    @PostMapping("/api/projects")
    fun createProject(
        @AuthenticationPrincipal userProfile: UserProfile
    ): ResponseDto<ProjectDetails> {
        val project = projectCreateUseCase.create(userProfile)
        return SuccessResponseDto(project)
    }

    @Secured(UserRole.teacher, UserRole.student)
    @GetMapping("/api/projects")
    fun listOwnProjects(
        @AuthenticationPrincipal userProfile: UserProfile,
        pageable: Pageable
    ): ResponseDto<List<ProjectCard>> {
        val projects = projectListUseCase.list(userProfile, UnpagedSorted(pageable))
        return SuccessResponseDto(projects.content) // TODO: return page
    }

    @Secured(UserRole.teacher, UserRole.student)
    @PutMapping("/api/projects/{projectId}")
    fun updateProject(
        @PathVariable projectId: UUID,
        @RequestBody projectUpdateRequest: ProjectUpdateRequest,
        @AuthenticationPrincipal userProfile: UserProfile
    ): ResponseEntity<ResponseDto<ProjectDetails?>> {
        return when (val result = projectUpdateUseCase.update(userProfile, projectId, projectUpdateRequest)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @Secured(UserRole.teacher, UserRole.student)
    @PostMapping("/api/projects/{projectId}/run-result")
    fun runProject(
        @PathVariable projectId: UUID,
        @AuthenticationPrincipal userProfile: UserProfile
    ): ResponseEntity<ResponseDto<RunCodeResponse>> {
        return when (val result = projectRunUseCase.run(userProfile, projectId)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @Secured(UserRole.teacher, UserRole.student)
    @GetMapping("/api/projects/{projectId}")
    fun getProject(
        @PathVariable projectId: UUID,
        @AuthenticationPrincipal userProfile: UserProfile
    ): ResponseEntity<ResponseDto<ProjectDetails>> {
        return when (val result = projectGetUseCase.get(userProfile, projectId)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @Secured(UserRole.teacher, UserRole.student)
    @DeleteMapping("/api/projects/{projectId}")
    fun deleteProject(
        @PathVariable projectId: UUID,
        @AuthenticationPrincipal userProfile: UserProfile
    ): ResponseEntity<ResponseDto<Void>> {
        return when (val result = projectDeleteUseCase.delete(userProfile, projectId)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok().build()
        }
    }

}
