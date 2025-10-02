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
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.stereotype.Service
import java.util.*

interface InvitationCoTeacherResendUseCase {

    fun resend(invitationId: UUID, currentTeacher: TeacherProfile): Either<OperationError, Unit>
}

@Service
class InvitationCoTeacherResendUseCaseImpl(
    private val coTeacherService: CoTeacherService,
    private val classroomService: ClassroomService,
    private val invitationCoTeacherEmailSender: InvitationCoTeacherEmailSender
): InvitationCoTeacherResendUseCase {

    override fun resend(invitationId: UUID, currentTeacher: TeacherProfile): Either<OperationError, Unit> {

        val coTeacher = coTeacherService.getCoTeacher(invitationId)
            ?: return NotFoundError("INVITATION_NOT_FOUND").left()

        if (coTeacher.coTeacherStatus == TeacherRole.CO_TEACHER)
            return ValidationError("INVITATION_ALREADY_ACCEPTED").left()

        val classroom = classroomService.getById(coTeacher.classroomId)
            ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()

        if (classroom.blocked)
            return AccessDeniedError("SUBSCRIPTION_REQUIRED").left()

        if (currentTeacher.id != classroom.teacherId)
            return AccessDeniedError("TEACHER_SHOULD_BE_OWNER_FOR_RESEND_INVITATION").left()

        val preferences = InvitationEmailPreferences(
            fromName = currentTeacher.fullName,
            from = currentTeacher.email,
            sendTo = coTeacher.teacherEmail,
            invitationId = coTeacher.id
        )

        invitationCoTeacherEmailSender.send(preferences)

        return Unit.right()
    }
}
