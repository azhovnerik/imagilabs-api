package com.anahoret.imagilabsapi.coteachers.web

import arrow.core.Either
import com.anahoret.imagilabsapi.common.web.EmptySuccessResponseDto
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.common.web.mapErrors
import com.anahoret.imagilabsapi.coteachers.domain.*
import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class CoTeachersController(
    private val invitationCoTeacherUseCase: InvitationCoTeacherUseCase,
    private val invitationCoTeacherAcceptUseCase: InvitationCoTeacherAcceptUseCase,
    private val coTeachersByClassroomIdUseCase: CoTeachersClassroomIdUseCase,
    private val coTeacherRemoveUseCase: CoTeacherRemoveUseCase
) {

    @Secured(UserRole.teacher, UserRole.student)
    @GetMapping("/api/classrooms/{classroomId}/co-teachers")
    fun getAllCoTeachers(
        @PathVariable classroomId: UUID
    ): ResponseEntity<ResponseDto<List<CoTeacher>>> {
        return when (val result = coTeachersByClassroomIdUseCase.getAll(classroomId)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @Secured(UserRole.teacher)
    @PostMapping("/api/classrooms/{classroomId}/invite-teacher")
    fun inviteTeacher(
        @PathVariable classroomId: UUID,
        @RequestBody request: InvitationCoTeacherRequest,
        @AuthenticationPrincipal teacherProfile: TeacherProfile
    ): ResponseEntity<ResponseDto<CoTeacher>> {
        return when (val result = invitationCoTeacherUseCase.invite(classroomId, request, teacherProfile.id)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @Secured(UserRole.teacher)
    @PutMapping("/api/classrooms/{invitationId}/accept-invite")
    fun acceptInvitation(
        @PathVariable invitationId: UUID,
        @AuthenticationPrincipal teacherProfile: TeacherProfile
    ): ResponseEntity<ResponseDto<Void>> {
        return when (val result = invitationCoTeacherAcceptUseCase.accept(invitationId, teacherProfile)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(EmptySuccessResponseDto)
        }
    }

    @Secured(UserRole.teacher)
    @DeleteMapping("/api/classrooms/{classroomId}/{coTeacherId}/remove")
    fun removeCoTeacher(
        @PathVariable classroomId: UUID,
        @PathVariable coTeacherId: UUID,
        @AuthenticationPrincipal teacherProfile: TeacherProfile
    ): ResponseEntity<ResponseDto<Void>> {
        return when (val result = coTeacherRemoveUseCase.remove(classroomId, coTeacherId, teacherProfile.id)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(EmptySuccessResponseDto)
        }
    }
}
