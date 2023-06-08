package com.anahoret.imagilabsapi.projects.web

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.common.web.mapErrors
import com.anahoret.imagilabsapi.projects.domain.ProjectCard
import com.anahoret.imagilabsapi.projects.domain.ProjectDetails
import com.anahoret.imagilabsapi.projects.domain.ProjectUpdateRequest
import com.anahoret.imagilabsapi.projects.domain.SearchProjectsRequest
import com.anahoret.imagilabsapi.projects.domain.usecases.*
import com.anahoret.imagilabsapi.security.UserRole
import org.springframework.data.domain.Page
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
    @PostMapping("/api/classrooms/{classroomId}/projects/search")
    fun listProjects(
        @PathVariable classroomId: UUID,
        @RequestBody searchRequest: SearchProjectsRequest,
        @AuthenticationPrincipal userProfile: UserProfile,
        pageable: Pageable
    ): ResponseEntity<ResponseDto<Page<ProjectCard>>> {
        return when (val result = projectListUseCase.list(classroomId, userProfile, searchRequest, pageable)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @Secured(UserRole.teacher, UserRole.student)
    @PutMapping("/api/classrooms/{classroomId}/projects/{projectId}")
    fun updateProject(
        @PathVariable classroomId: UUID,
        @PathVariable projectId: UUID,
        @RequestBody projectUpdateRequest: ProjectUpdateRequest,
        @AuthenticationPrincipal userProfile: UserProfile
    ): ResponseEntity<ResponseDto<ProjectDetails?>> {
        return when (val result = projectUpdateUseCase.update(classroomId, userProfile, projectId, projectUpdateRequest)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @Secured(UserRole.teacher, UserRole.student)
    @GetMapping("/api/classrooms/{classroomId}/projects/{projectId}")
    fun getProject(
        @PathVariable classroomId: UUID,
        @PathVariable projectId: UUID,
        @AuthenticationPrincipal userProfile: UserProfile
    ): ResponseEntity<ResponseDto<ProjectDetails>> {
        return when (val result = projectGetUseCase.get(classroomId, userProfile, projectId)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @Secured(UserRole.teacher, UserRole.student)
    @DeleteMapping("/api/classrooms/{classroomId}/projects/{projectId}")
    fun deleteProject(
        @PathVariable classroomId: UUID,
        @PathVariable projectId: UUID,
        @AuthenticationPrincipal userProfile: UserProfile
    ): ResponseEntity<ResponseDto<Void>> {
        return when (val result = projectDeleteUseCase.delete(classroomId, userProfile, projectId)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok().build()
        }
    }

}
