package com.anahoret.imagilabsapi.teachingmaterials.web

import arrow.core.Either
import com.anahoret.imagilabsapi.common.web.EmptySuccessResponseDto
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.common.web.mapErrors
import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.teachingmaterials.domain.TeacherLesson
import com.anahoret.imagilabsapi.teachingmaterials.domain.TeacherLessonsGetUseCase
import com.anahoret.imagilabsapi.teachingmaterials.domain.TeacherLessonsUpdateRequest
import com.anahoret.imagilabsapi.teachingmaterials.domain.TeacherLessonsUpdateUseCase
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class LessonsController(
    private val teacherLessonsUpdateUseCase: TeacherLessonsUpdateUseCase,
    private val teacherLessonsGetUseCase: TeacherLessonsGetUseCase
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

    @Secured(UserRole.admin)
    @GetMapping("/api/teachers/{teacherId}/lessons")
    fun getLessonsByTeacher(
        @PathVariable teacherId: UUID
    ): ResponseEntity<ResponseDto<List<TeacherLesson>>> {
        return ResponseEntity.ok(SuccessResponseDto(teacherLessonsGetUseCase.get(teacherId)))
    }

}
