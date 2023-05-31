package com.anahoret.imagilabsapi.teachers.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomDeleteUseCase
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.projects.domain.ProjectService
import com.anahoret.imagilabsapi.teachingmaterials.domain.TeacherBundleService
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.*


interface TeacherDeleteUseCase {

    fun delete(deleteBy: TeacherProfile, teacherId: UUID): Either<OperationError, Unit>
}

@Service
class TeacherDeleteUseCaseImpl(
    private val teacherAccessService: TeacherAccessService,
    private val teacherProfileService: TeacherProfileService,
    private val classroomDeleteUseCase: ClassroomDeleteUseCase,
    private val projectService: ProjectService,
    private val classroomService: ClassroomService,
    private val teacherBundleService: TeacherBundleService
) : TeacherDeleteUseCase {

    @Transactional(rollbackOn = [Throwable::class])
    override fun delete(deleteBy: TeacherProfile, teacherId: UUID): Either<OperationError, Unit> {
        val teacherProfile = teacherProfileService.getTeacherById(teacherId)
            ?: return NotFoundError("TEACHER_NOT_FOUND").left()
        if (!teacherAccessService.canDelete(deleteBy, teacherProfile))
            return AccessDeniedError("ACCESS_TO_TEACHER_DENIED").left()

        deleteClassrooms(teacherProfile)
        deleteTeacherProjects(teacherProfile.id)
        teacherBundleService.deleteAllByTeacherId(teacherProfile.id)
        teacherProfileService.delete(teacherProfile.id)
        return Unit.right()
    }

    //following method will also delete students from the classrooms and their projects (shared and drafts)
    private fun deleteClassrooms(teacherProfile: TeacherProfile) {
        val teacherClassroomIds = classroomService.listByTeacher(teacherProfile.id)
            .map { it.id }
        for (classroomId in teacherClassroomIds) {
            classroomDeleteUseCase.delete(teacherProfile, classroomId)
        }
    }

    private fun deleteTeacherProjects(teacherId: UUID) {
        projectService.deleteAllByOwner(teacherId)
    }
}

