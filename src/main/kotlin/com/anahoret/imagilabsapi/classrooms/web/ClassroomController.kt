package com.anahoret.imagilabsapi.classrooms.web

import arrow.core.Either
import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomCreateRequest
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomCreateUseCase
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.common.web.toBadRequestResponse
import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ClassroomController(
    private val classroomCreateUseCase: ClassroomCreateUseCase
) {

    @Secured(UserRole.teacher)
    @PostMapping("/api/classrooms")
    fun createClassroom(
        @RequestBody classroomCreateRequest: ClassroomCreateRequest,
        @AuthenticationPrincipal teacherProfile: TeacherProfile
    ): ResponseEntity<ResponseDto<Classroom?>> {
        return when (val createResult = classroomCreateUseCase.create(teacherProfile, classroomCreateRequest)) {
            is Either.Left -> createResult.value.toBadRequestResponse()
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(createResult.value))
        }
    }

}
