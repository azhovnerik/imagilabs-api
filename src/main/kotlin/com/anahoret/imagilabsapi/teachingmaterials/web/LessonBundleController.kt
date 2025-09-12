package com.anahoret.imagilabsapi.teachingmaterials.web

import arrow.core.Either
import com.anahoret.imagilabsapi.common.web.EmptySuccessResponseDto
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.common.web.mapErrors
import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.teachingmaterials.domain.*
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class LessonBundleController(
    private val lessonBundleCreateUseCase: LessonBundleCreateUseCase,
    private val lessonBundleUpdateUseCase: LessonBundleUpdateUseCase,
    private val lessonBundleGetUseCase: LessonBundleGetUseCase,
    private val lessonBundleService: LessonBundleService
) {

    @Secured(UserRole.ADMIN)
    @PostMapping("/api/lessons/bundles")
    fun createBundle(
        @RequestBody lessonBundleDataRequest: LessonBundleDataRequest
    ): ResponseEntity<ResponseDto<LessonBundle>> {
        return when (val result = lessonBundleCreateUseCase.create(lessonBundleDataRequest)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @Secured(UserRole.ADMIN)
    @GetMapping("/api/lessons/bundles/{bundleId}")
    fun getBundle(@PathVariable bundleId: UUID): ResponseEntity<ResponseDto<LessonBundle?>> {
        return when (val result = lessonBundleGetUseCase.get(bundleId)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @Secured(UserRole.ADMIN)
    @GetMapping("/api/lessons/bundles")
    fun getBundles(
        @RequestParam(required = false) searchQuery: String?,
        sort: Sort
    ): ResponseDto<List<LessonBundleBase>> {
        return SuccessResponseDto(lessonBundleService.list(searchQuery, sort))
    }

    @Secured(UserRole.ADMIN)
    @PutMapping("/api/lessons/bundles/{bundleId}")
    fun updateBundle(
        @PathVariable bundleId: UUID,
        @RequestBody lessonBundleDataRequest: LessonBundleDataRequest
    ): ResponseEntity<ResponseDto<LessonBundle>> {
        return when (val result = lessonBundleUpdateUseCase.update(bundleId, lessonBundleDataRequest)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @Secured(UserRole.ADMIN)
    @DeleteMapping("/api/lessons/bundles/{bundleId}")
    fun deleteBundle(@PathVariable bundleId: UUID): ResponseDto<Void> {
        lessonBundleService.delete(bundleId)
        return EmptySuccessResponseDto
    }

}
