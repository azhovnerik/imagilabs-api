package com.anahoret.imagilabsapi.coteachers.web

import arrow.core.Either
import com.anahoret.imagilabsapi.common.web.EmptySuccessResponseDto
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.mapErrors
import com.anahoret.imagilabsapi.coteachers.domain.InvitationCoTeacherAcceptUseCase
import com.anahoret.imagilabsapi.coteachers.domain.InvitationCoTeacherRequest
import com.anahoret.imagilabsapi.coteachers.domain.InvitationCoTeacherUseCase
import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class CoTeachersController(
    private val invitationCoTeacherUseCase: InvitationCoTeacherUseCase,
    private val invitationCoTeacherAcceptUseCase: InvitationCoTeacherAcceptUseCase
) {

    @Secured(UserRole.teacher)
    @PostMapping("/api/classrooms/{classroomId}/invite-teacher")
    fun inviteTeacher(
        @PathVariable classroomId: UUID,
        @RequestBody request: InvitationCoTeacherRequest,
        @AuthenticationPrincipal teacherProfile: TeacherProfile
    ): ResponseEntity<ResponseDto<Void>> {
        return when (val result = invitationCoTeacherUseCase.invite(classroomId, request, teacherProfile.id)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(EmptySuccessResponseDto)
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
}
