package com.anahoret.imagilabsapi.coteachers.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomTeacher
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.signup.domain.ImagiLabsEmailValidator
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import org.springframework.stereotype.Service
import java.util.*

interface InvitationCoTeacherUseCase {

    fun invite(
        classroomId: UUID,
        request: InvitationCoTeacherRequest,
        currentTeacher: TeacherProfile
    ): Either<OperationError, ClassroomTeacher>
}

@Service
class InvitationCoTeacherUseCaseImpl(
    private val emailValidator: ImagiLabsEmailValidator,
    private val classroomService: ClassroomService,
    private val coTeacherService: CoTeacherService,
    private val teacherProfileService: TeacherProfileService,
    private val invitationCoTeacherEmailSender: InvitationCoTeacherEmailSender
) : InvitationCoTeacherUseCase {

    override fun invite(
        classroomId: UUID,
        request: InvitationCoTeacherRequest,
        currentTeacher: TeacherProfile
    ): Either<OperationError, ClassroomTeacher> {

        with(request.normalized()) {
            if (!emailValidator.isValid(teacherEmail))
                return ValidationError("EMAIL_NOT_VALID").left()

            val classroom = classroomService.getById(classroomId)
                ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()

            if (currentTeacher.id != classroom.teacherId)
                return AccessDeniedError("TEACHER_SHOULD_BE_OWNER_FOR_INVITATION").left()

            if (coTeacherService.getCoTeacherCountByClassroomId(classroomId) >= 5)
                return ValidationError("CO_TEACHERS_LIMIT_EXCEEDED").left()

            if (currentTeacher.email == teacherEmail)
                return ValidationError("TEACHER_CANNOT_INVITE_HIMSELF").left()

            if (coTeacherService.isExistsPendingInvite(classroomId, teacherEmail))
                return ValidationError("INVITE_IS_ALREADY_EXISTS").left()

            val teacherId = teacherProfileService.getTeacherIdByEmail(request.teacherEmail)
            val coTeacher = coTeacherService.createCoTeacher(classroomId, teacherEmail, teacherId)

            invitationCoTeacherEmailSender.send(teacherEmail, coTeacher.id)

            return ClassroomTeacher.mapFromCoTeacher(coTeacher, false).right()
        }
    }
}
