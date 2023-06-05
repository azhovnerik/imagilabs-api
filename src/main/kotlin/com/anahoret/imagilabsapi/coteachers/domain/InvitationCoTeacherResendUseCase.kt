package com.anahoret.imagilabsapi.coteachers.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.classrooms.domain.TeacherRole
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import org.springframework.stereotype.Service
import java.util.*

interface InvitationCoTeacherResendUseCase {

    fun resend(invitationId: UUID, currentTeacherId: UUID): Either<OperationError, Unit>
}

@Service
class InvitationCoTeacherResendUseCaseImpl(
    private val coTeacherService: CoTeacherService,
    private val classroomService: ClassroomService,
    private val invitationCoTeacherEmailSender: InvitationCoTeacherEmailSender
): InvitationCoTeacherResendUseCase {

    override fun resend(invitationId: UUID, currentTeacherId: UUID): Either<OperationError, Unit> {

        val coTeacher = coTeacherService.getCoTeacher(invitationId)
            ?: return NotFoundError("INVITATION_NOT_FOUND").left()

        if (coTeacher.coTeacherStatus == TeacherRole.CO_TEACHER)
            return ValidationError("INVITATION_ALREADY_ACCEPTED").left()

        val classroom = classroomService.getById(coTeacher.classroomId)
            ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()

        if (currentTeacherId != classroom.teacherId)
            return AccessDeniedError("TEACHER_SHOULD_BE_OWNER_FOR_RESEND_INVITATION").left()

        invitationCoTeacherEmailSender.send(coTeacher.teacherEmail, invitationId)

        return Unit.right()
    }
}
