package com.anahoret.imagilabsapi.teachingmaterials.web

import arrow.core.Either
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.common.web.mapErrors
import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.teachingmaterials.domain.LessonBundle
import com.anahoret.imagilabsapi.teachingmaterials.domain.LessonBundleCreateUseCase
import com.anahoret.imagilabsapi.teachingmaterials.domain.LessonBundleDataRequest
import com.anahoret.imagilabsapi.teachingmaterials.domain.LessonBundleUpdateUseCase
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class LessonBundleController(
    private val lessonBundleCreateUseCase: LessonBundleCreateUseCase,
    private val lessonBundleUpdateUseCase: LessonBundleUpdateUseCase
) {

    @Secured(UserRole.admin)
    @PostMapping("/api/lessons/bundles")
    fun createBundle(
        @RequestBody lessonBundleDataRequest: LessonBundleDataRequest
    ): ResponseEntity<ResponseDto<LessonBundle>> {
        return when (val result = lessonBundleCreateUseCase.create(lessonBundleDataRequest)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @Secured(UserRole.admin)
    @GetMapping("/api/lessons/bundles")
    fun getBundles(): ResponseDto<List<LessonBundle>> {
        TODO()
    }

    @Secured(UserRole.admin)
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

    @Secured(UserRole.admin)
    @DeleteMapping("/api/lessons/bundles/{bundleId}")
    fun deleteBundle(@PathVariable bundleId: UUID): ResponseEntity<ResponseDto<Void>> {
        TODO()
    }

}
