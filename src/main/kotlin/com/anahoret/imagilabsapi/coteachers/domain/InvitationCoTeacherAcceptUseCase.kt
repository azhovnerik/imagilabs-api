package com.anahoret.imagilabsapi.coteachers.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.classrooms.domain.TeacherRole
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.teacherchecklist.domain.CompleteTeacherCheckListStepUseCase
import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStep.CREATE_OR_JOIN_YOUR_FIRST_CLASSROOM
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.stereotype.Service
import java.util.*

interface InvitationCoTeacherAcceptUseCase {

    fun accept(coTeacherId: UUID, teacherProfile: TeacherProfile): Either<OperationError, Classroom>
}

@Service
class InvitationCoTeacherAcceptUseCaseImpl(
    private val coTeacherService: CoTeacherService,
    private val classroomService: ClassroomService,
    private val completeTeacherCheckListStepUseCase: CompleteTeacherCheckListStepUseCase
) : InvitationCoTeacherAcceptUseCase {

    override fun accept(
        coTeacherId: UUID,
        teacherProfile: TeacherProfile
    ): Either<OperationError, Classroom> {

        val coTeacher = coTeacherService.getCoTeacher(coTeacherId)
            ?: return NotFoundError("INVITATION_NOT_FOUND").left()

        if (teacherProfile.email != coTeacher.teacherEmail)
            return AccessDeniedError("ONLY_INVITED_TEACHER_CAN_ACCEPT_INVITATION").left()

        val classroom = classroomService.getById(coTeacher.classroomId)
            ?.let { it.teacherRole = TeacherRole.CO_TEACHER; it}
            ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()

        if (coTeacher.teacherId == null) coTeacherService.acceptInvitation(coTeacherId, teacherProfile.id)
        else coTeacherService.acceptInvitation(coTeacherId)

        completeTeacherCheckListStepUseCase.complete(teacherProfile.id, CREATE_OR_JOIN_YOUR_FIRST_CLASSROOM)

        return classroom.right()
    }
}
