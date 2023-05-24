package com.anahoret.imagilabsapi.coteachers.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.stereotype.Service
import java.util.*

interface InvitationCoTeacherAcceptUseCase {

    fun accept(invitationId: UUID, teacherProfile: TeacherProfile): Either<OperationError, Unit>
}

@Service
class InvitationCoTeacherAcceptUseCaseImpl(
    private val coTeacherService: CoTeacherService
): InvitationCoTeacherAcceptUseCase {

    override fun accept(
        invitationId: UUID,
        teacherProfile: TeacherProfile
    ): Either<OperationError, Unit> {

        val coTeacher = coTeacherService.getCoTeacher(invitationId)
            ?: return NotFoundError("INVITATION_NOT_FOUND").left()

        if (teacherProfile.email != coTeacher.teacherEmail)
            return AccessDeniedError("INVITED_TEACHER_SHOULD_ACCEPT_INVITATION").left()

        coTeacherService.setTeacherIdByEmail(invitationId, teacherProfile.id)

        return Unit.right()
    }
}
