package com.anahoret.imagilabsapi.coteachers.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.stereotype.Service
import java.util.*

interface InvitationCoTeacherAcceptUseCase {

    fun accept(coTeacherId: UUID, teacherProfile: TeacherProfile): Either<OperationError, Unit>
}

@Service
class InvitationCoTeacherAcceptUseCaseImpl(
    private val coTeacherService: CoTeacherService
): InvitationCoTeacherAcceptUseCase {

    override fun accept(
        coTeacherId: UUID,
        teacherProfile: TeacherProfile
    ): Either<OperationError, Unit> {

        val coTeacher = coTeacherService.getCoTeacher(coTeacherId)
            ?: return NotFoundError("INVITATION_NOT_FOUND").left()

        if (teacherProfile.email != coTeacher.teacherEmail)
            return AccessDeniedError("ONLY_INVITED_TEACHER_CAN_ACCEPT_INVITATION").left()

        val teacherName = with(teacherProfile) { "$firstName $lastName" }
        coTeacherService.setTeacherIdAndName(coTeacherId, teacherProfile.id, teacherName)

        return Unit.right()
    }
}
