package com.anahoret.imagilabsapi.classrooms.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.projectclassroomshare.domain.ProjectClassroomShareService
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import com.anahoret.imagilabsapi.students.domain.StudentClassroomCard
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.userclassroomlink.domain.StudentClassroomLink
import com.anahoret.imagilabsapi.userclassroomlink.domain.StudentClassroomLinkService
import org.springframework.stereotype.Service
import java.util.*

interface ListStudentsInClassroomUseCase {

    fun list(listBy: TeacherProfile, classroomId: UUID): Either<OperationError, List<StudentClassroomCard>>
}

@Service
class ListStudentsInClassroomUseCaseImpl(
    private val classroomService: ClassroomService,
    private val studentClassroomLinkService: StudentClassroomLinkService,
    private val studentProfileService: StudentProfileService,
    private val projectService: ProjectService,
    private val projectClassroomShareService: ProjectClassroomShareService,
    private val classroomAccessService: ClassroomAccessService
) : ListStudentsInClassroomUseCase {

    override fun list(listBy: TeacherProfile, classroomId: UUID): Either<OperationError, List<StudentClassroomCard>> {
        val classroom = classroomService.getById(classroomId) ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()
        if (!classroomAccessService.canListStudentCredentials(listBy, classroom))
            return AccessDeniedError("ACCESS_TO_CLASSROOM_DENIED").left()

        val studentIds = studentClassroomLinkService.listByClassroom(classroom.id)
            .map(StudentClassroomLink::studentId)

        val projectCounts = projectService.getProjectCounts(studentIds)
        val sharedProjectCounts = projectClassroomShareService.getProjectCountsByOwners(studentIds)

        return studentProfileService.listStudentCredentialsCardsByIds(
            studentIds,
            classroom.accessCode,
            projectCounts,
            sharedProjectCounts
        ).right()
    }
}
