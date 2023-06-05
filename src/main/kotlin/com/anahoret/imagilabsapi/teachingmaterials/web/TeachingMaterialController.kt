package com.anahoret.imagilabsapi.teachingmaterials.web

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.common.web.mapErrors
import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.teachingmaterials.domain.TeachingMaterials
import com.anahoret.imagilabsapi.teachingmaterials.domain.ClassroomTeachingMaterialsGetUseCase
import com.anahoret.imagilabsapi.teachingmaterials.domain.TeachingMaterialsGetUseCase
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class TeachingMaterialController(
    private val classroomTeachingMaterialsGetUseCase: ClassroomTeachingMaterialsGetUseCase,
    private val teachingMaterialsGetUseCase: TeachingMaterialsGetUseCase
) {

    @Secured(UserRole.teacher, UserRole.student)
    @GetMapping("/api/classrooms/{classroomId}/teaching-materials")
    fun classroomMaterials(
        @PathVariable classroomId: UUID,
        @AuthenticationPrincipal userProfile: UserProfile
    ): ResponseEntity<ResponseDto<TeachingMaterials?>> {
        return when (val result = classroomTeachingMaterialsGetUseCase.get(userProfile, classroomId)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @Secured(UserRole.teacher)
    @GetMapping("/api/teaching-materials")
    fun teacherMaterials(
        @AuthenticationPrincipal teacherProfile: TeacherProfile
    ): ResponseEntity<ResponseDto<TeachingMaterials?>> {
        return when (val result = teachingMaterialsGetUseCase.get(teacherProfile.id)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

}
