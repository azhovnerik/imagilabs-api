package com.anahoret.imagilabsapi.teachingmaterials.web

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.web.EmptySuccessResponseDto
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.common.web.mapErrors
import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.teachingmaterials.domain.*
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class TeacherBundleController(
    private val teacherBundleCreateUseCase: TeacherBundleCreateUseCase,
    private val teacherBundlesGetUseCase: TeacherBundlesGetUseCase,
    private val teacherBundleDeleteUseCase: TeacherBundleDeleteUseCase
) {

    @Secured(UserRole.admin)
    @PostMapping("/api/teachers/{teacherId}/bundles")
    fun addBundleToTeacher(
        @PathVariable teacherId: UUID,
        @RequestBody request: TeacherBundleCreateRequest
    ): ResponseEntity<ResponseDto<Void>> {
        return when (val result = teacherBundleCreateUseCase.create(teacherId, request.bundleId)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(EmptySuccessResponseDto)
        }
    }

    @Secured(UserRole.admin, UserRole.teacher)
    @GetMapping("/api/teachers/{teacherId}/bundles")
    fun getTeacherBundles(
        @PathVariable teacherId: UUID,
        @AuthenticationPrincipal userProfile: UserProfile
    ): ResponseEntity<ResponseDto<List<LessonBundle>>> {
        return when (val result = teacherBundlesGetUseCase.getAll(teacherId, userProfile)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @Secured(UserRole.admin)
    @DeleteMapping("/api/teachers/{teacherId}/bundles/{bundleId}")
    fun deleteTeacherBundle(
        @PathVariable teacherId: UUID,
        @PathVariable bundleId: UUID
    ): ResponseEntity<ResponseDto<Void>> {
        return when (val result = teacherBundleDeleteUseCase.delete(teacherId, bundleId)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(EmptySuccessResponseDto)
        }
    }
}
