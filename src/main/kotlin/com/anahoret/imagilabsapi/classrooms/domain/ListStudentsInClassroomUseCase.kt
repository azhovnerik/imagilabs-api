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
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.userclassroomlink.domain.StudentClassroomLinkService
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.*

interface ListStudentsInClassroomUseCase {

    fun list(listBy: TeacherProfile, classroomId: UUID): Either<OperationError, List<StudentClassroomCard>>
    fun list(
        listBy: TeacherProfile,
        classroomId: UUID,
        searchQuery: String?,
        sort: Sort
    ): Either<OperationError, List<StudentClassroomCard>>
}

@Service
class ListStudentsInClassroomUseCaseImpl(
    private val classroomService: ClassroomService,
    private val studentProfileService: StudentProfileService,
    private val projectService: ProjectService,
    private val projectClassroomShareService: ProjectClassroomShareService,
    private val classroomAccessService: ClassroomAccessService,
    private val studentClassroomLinkService: StudentClassroomLinkService
) : ListStudentsInClassroomUseCase {

    override fun list(listBy: TeacherProfile, classroomId: UUID): Either<OperationError, List<StudentClassroomCard>> {
        return doList(listBy, classroomId, searchQuery = null, Sort.unsorted())
    }

    override fun list(
        listBy: TeacherProfile,
        classroomId: UUID,
        searchQuery: String?,
        sort: Sort
    ): Either<OperationError, List<StudentClassroomCard>> {
        return doList(listBy, classroomId, searchQuery, sort)
    }

    private fun doList(
        listBy: TeacherProfile,
        classroomId: UUID,
        searchQuery: String?,
        sort: Sort
    ): Either<OperationError, List<StudentClassroomCard>> {
        val classroom = classroomService.getById(classroomId) ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()
        if (!classroomAccessService.canListStudentCredentials(listBy, classroom))
            return AccessDeniedError("ACCESS_TO_CLASSROOM_DENIED").left()

        val studentIdsPage = studentClassroomLinkService.listStudentsByClassroom(classroom.id, searchQuery, sort)
            .map(StudentProfile::id)

        val projectCounts = projectService.getProjectCounts(studentIdsPage)
        val sharedProjectCounts = projectClassroomShareService.getProjectCountsByOwners(studentIdsPage)

        val studentClassroomCards = studentProfileService.listStudentCredentialsCardsByIds(
            studentIdsPage,
            classroom,
            projectCounts,
            sharedProjectCounts
        ).sortedBy { studentIdsPage.indexOf(it.id) }
        return studentClassroomCards.right()
    }

}
