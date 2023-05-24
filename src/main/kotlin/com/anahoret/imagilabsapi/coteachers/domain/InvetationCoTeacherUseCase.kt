package com.anahoret.imagilabsapi.coteachers.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.signup.domain.ImagiLabsEmailValidator
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.stereotype.Service
import java.util.*

interface InvitationCoTeacherUseCase {

    fun invite(
        classroomId: UUID,
        request: InvitationCoTeacherRequest,
        currentTeacherId: UUID
    ): Either<OperationError, Unit>
}

@Service
class InvitationCoTeacherUseCaseImpl(
    private val emailValidator: ImagiLabsEmailValidator,
    private val classroomService: ClassroomService,
    private val coTeacherService: CoTeacherService,
    private val invitationCoTeacherEmailSender: InvitationCoTeacherEmailSender
) : InvitationCoTeacherUseCase {

    override fun invite(
        classroomId: UUID,
        request: InvitationCoTeacherRequest,
        currentTeacherId: UUID
    ): Either<OperationError, Unit> {

        with(request.normalized()) {
            if (!emailValidator.isValid(teacherEmail))
                return ValidationError("EMAIL_NOT_VALID").left()

            val classroom = classroomService.getById(classroomId)
                ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()

            if (currentTeacherId != classroom.teacherId)
                return AccessDeniedError("TEACHER_SHOULD_BE_OWNER_FOR_INVITATION").left()

            coTeacherService.createCoTeacher(classroomId, teacherEmail)
            invitationCoTeacherEmailSender.send(teacherEmail, classroomId)

            return Unit.right()
        }
    }
}
