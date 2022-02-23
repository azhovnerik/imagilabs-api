package com.anahoret.imagilabsapi.classrooms.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.studentclassroomlink.domain.StudentClassroomLink
import com.anahoret.imagilabsapi.studentclassroomlink.domain.StudentClassroomLinkService
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.stereotype.Service
import java.util.*

interface ListStudentsInClassroomUseCase {

    fun list(listBy: TeacherProfile, classroomId: UUID): Either<OperationError, List<StudentProfile>>
}

@Service
class ListStudentsInClassroomUseCaseImpl(
    private val classroomService: ClassroomService,
    private val studentClassroomLinkService: StudentClassroomLinkService,
    private val studentProfileService: StudentProfileService
) : ListStudentsInClassroomUseCase {

    override fun list(listBy: TeacherProfile, classroomId: UUID): Either<OperationError, List<StudentProfile>> {
        val classroom = classroomService.getById(classroomId) ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()
        if (classroom.teacherId != listBy.id) return AccessDeniedError("ACCESS_TO_CLASSROOM_DENIED").left()

        return studentClassroomLinkService.listByClassroom(classroom.id)
            .map(StudentClassroomLink::studentId)
            .let(studentProfileService::listByIds)
            .right()
    }
}
