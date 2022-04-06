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
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.*

interface ListStudentsInClassroomUseCase {

    fun list(listBy: TeacherProfile, classroomId: UUID): Either<OperationError, Page<StudentClassroomCard>>
    fun list(
        listBy: TeacherProfile,
        classroomId: UUID,
        searchQuery: String?,
        pageable: Pageable
    ): Either<OperationError, Page<StudentClassroomCard>>
}

@Service
class ListStudentsInClassroomUseCaseImpl(
    private val classroomService: ClassroomService,
    private val studentProfileService: StudentProfileService,
    private val projectService: ProjectService,
    private val projectClassroomShareService: ProjectClassroomShareService,
    private val classroomAccessService: ClassroomAccessService
) : ListStudentsInClassroomUseCase {

    override fun list(listBy: TeacherProfile, classroomId: UUID): Either<OperationError, Page<StudentClassroomCard>> {
        return doList(listBy, classroomId, searchQuery = null, Pageable.unpaged())
    }

    override fun list(
        listBy: TeacherProfile,
        classroomId: UUID,
        searchQuery: String?,
        pageable: Pageable
    ): Either<OperationError, Page<StudentClassroomCard>> {
        return doList(listBy, classroomId, searchQuery, pageable)
    }

    private fun doList(
        listBy: TeacherProfile,
        classroomId: UUID,
        searchQuery: String?,
        pageable: Pageable
    ): Either<OperationError, Page<StudentClassroomCard>> {
        val classroom = classroomService.getById(classroomId) ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()
        if (!classroomAccessService.canListStudentCredentials(listBy, classroom))
            return AccessDeniedError("ACCESS_TO_CLASSROOM_DENIED").left()

        val studentIdsPage = studentProfileService.listByClassroom(classroom.id, searchQuery, pageable)
            .map(StudentProfile::id)
        val studentIds = studentIdsPage.content

        val projectCounts = projectService.getProjectCounts(studentIds)
        val sharedProjectCounts = projectClassroomShareService.getProjectCountsByOwners(studentIds)

        val studentClassroomCards = studentProfileService.listStudentCredentialsCardsByIds(
            studentIds,
            classroom,
            projectCounts,
            sharedProjectCounts
        ).sortedBy { studentIds.indexOf(it.id) }
        return if (pageable.isPaged) {
            PageImpl(studentClassroomCards, pageable, studentIdsPage.totalElements).right()
        } else {
            PageImpl(studentClassroomCards).right()
        }
    }

}
