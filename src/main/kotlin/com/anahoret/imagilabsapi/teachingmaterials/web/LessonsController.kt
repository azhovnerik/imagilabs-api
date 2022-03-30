package com.anahoret.imagilabsapi.teachingmaterials.web

import arrow.core.Either
import com.anahoret.imagilabsapi.common.web.EmptySuccessResponseDto
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.mapErrors
import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.teachingmaterials.domain.TeacherLessonsUpdateRequest
import com.anahoret.imagilabsapi.teachingmaterials.domain.TeacherLessonsUpdateUseCase
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class LessonsController(
    private val teacherLessonsUpdateUseCase: TeacherLessonsUpdateUseCase
) {

    @Secured(UserRole.admin)
    @PutMapping("/api/teachers/{teacherId}/lessons")
    fun updateLessonsForTeacher(
        @PathVariable teacherId: UUID,
        @RequestBody teacherLessonsUpdateRequest: TeacherLessonsUpdateRequest
    ): ResponseEntity<ResponseDto<Void>> {
        return when (val result = teacherLessonsUpdateUseCase.update(teacherId, teacherLessonsUpdateRequest)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(EmptySuccessResponseDto)
        }
    }

}
