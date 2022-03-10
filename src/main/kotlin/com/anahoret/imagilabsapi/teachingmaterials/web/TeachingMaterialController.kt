package com.anahoret.imagilabsapi.teachingmaterials.web

import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.teachingmaterials.domain.TeachingMaterialService
import com.anahoret.imagilabsapi.teachingmaterials.domain.TeachingMaterials
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TeachingMaterialController(
    private val teachingMaterialService: TeachingMaterialService
) {

    @GetMapping("/api/teaching-materials")
    fun list(): ResponseDto<TeachingMaterials> {
        return SuccessResponseDto(teachingMaterialService.listSortedByIndex())
    }

}
