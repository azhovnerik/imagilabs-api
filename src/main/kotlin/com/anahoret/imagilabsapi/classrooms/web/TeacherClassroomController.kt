package com.anahoret.imagilabsapi.classrooms.web

import arrow.core.Either
import com.anahoret.imagilabsapi.classrooms.domain.*
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.common.web.*
import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class TeacherClassroomController(
    private val classroomCreateUseCase: ClassroomCreateUseCase,
    private val classroomService: ClassroomService,
    private val listStudentsInClassroomUseCase: ListStudentsInClassroomUseCase
) {

    @Secured(UserRole.teacher)
    @PostMapping("/api/teacher/classrooms")
    fun createClassroom(
        @RequestBody classroomCreateRequest: ClassroomCreateRequest,
        @AuthenticationPrincipal teacherProfile: TeacherProfile
    ): ResponseEntity<ResponseDto<Classroom?>> {
        return when (val createResult = classroomCreateUseCase.create(teacherProfile, classroomCreateRequest)) {
            is Either.Left -> createResult.value.toBadRequestResponse()
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(createResult.value))
        }
    }

    @Secured(UserRole.teacher)
    @GetMapping("/api/teacher/classrooms")
    fun listClassrooms(@AuthenticationPrincipal teacherProfile: TeacherProfile): ResponseDto<List<Classroom>> {
        return SuccessResponseDto(classroomService.listByTeacher(teacherProfile.id))
    }

    @Secured(UserRole.teacher)
    @GetMapping("/api/teacher/classrooms/{classroomId}/students")
    fun listStudentsInClassroom(
        @PathVariable classroomId: UUID,
        @AuthenticationPrincipal teacherProfile: TeacherProfile
    ): ResponseEntity<ResponseDto<List<StudentProfile>?>> {
        return when (val result = listStudentsInClassroomUseCase.list(teacherProfile, classroomId)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    private fun mapErrors(operationError: OperationError): ResponseEntity<ResponseDto<List<StudentProfile>?>> {
        return when (operationError) {
            is NotFoundError -> operationError.toNotFoundResponse()
            is AccessDeniedError -> operationError.toForbiddenResponse()
            else -> unknownErrorResponse()
        }
    }

}
