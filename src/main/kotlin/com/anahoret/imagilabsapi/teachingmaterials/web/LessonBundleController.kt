package com.anahoret.imagilabsapi.teachingmaterials.web

import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.teachingmaterials.domain.LessonBundle
import com.anahoret.imagilabsapi.teachingmaterials.domain.LessonBundleDataRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class LessonBundleController {

    @Secured(UserRole.admin)
    @PostMapping("/api/lessons/bundles")
    fun createBundle(
        @RequestBody lessonBundleDataRequest: LessonBundleDataRequest
    ): ResponseEntity<ResponseDto<LessonBundle>> {
        TODO()
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
        TODO()
    }

    @Secured(UserRole.admin)
    @DeleteMapping("/api/lessons/bundles/{bundleId}")
    fun deleteBundle(@PathVariable bundleId: UUID): ResponseEntity<ResponseDto<Void>> {
        TODO()
    }

}
