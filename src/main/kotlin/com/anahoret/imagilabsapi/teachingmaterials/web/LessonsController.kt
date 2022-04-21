package com.anahoret.imagilabsapi.teachingmaterials.web

import arrow.core.Either
import com.anahoret.imagilabsapi.common.web.EmptySuccessResponseDto
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.common.web.mapErrors
import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.teachingmaterials.domain.*
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class LessonsController(
    private val teacherLessonsUpdateUseCase: TeacherLessonsUpdateUseCase,
    private val teacherLessonService: TeacherLessonService,
    private val lessonBundleAddToTeacherUseCase: LessonBundleAddToTeacherUseCase,
) {

    @Secured(UserRole.admin)
    @PutMapping("/api/teachers/{teacherId}/lessons")
    fun updateLessonsForTeacher(
        @PathVariable teacherId: UUID,
        @RequestBody teacherLessonsUpdateRequest: TeacherLessonsUpdateRequest
    ): ResponseEntity<ResponseDto<List<TeacherLesson>>> {
        return when (val result = teacherLessonsUpdateUseCase.update(teacherId, teacherLessonsUpdateRequest)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @Secured(UserRole.admin)
    @GetMapping("/api/teachers/{teacherId}/lessons")
    fun getLessonsByTeacher(
        @PathVariable teacherId: UUID
    ): ResponseDto<List<TeacherLesson>> {
        return SuccessResponseDto(teacherLessonService.listByTeacherId(teacherId))
    }

    @Secured(UserRole.admin)
    @PostMapping("/api/teachers/{teacherId}/lessons")
    fun addBundleToTeacher(
        @RequestParam bundleId: UUID,
        @PathVariable teacherId: UUID
    ): ResponseEntity<ResponseDto<Void>> {
        return when (val result = lessonBundleAddToTeacherUseCase.add(bundleId, teacherId)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(EmptySuccessResponseDto)
        }
    }

}
