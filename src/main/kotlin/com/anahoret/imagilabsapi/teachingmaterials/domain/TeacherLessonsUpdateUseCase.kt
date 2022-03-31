package com.anahoret.imagilabsapi.teachingmaterials.domain

import arrow.core.Either
import arrow.core.left
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.validation.ValidationErrors
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

interface TeacherLessonsUpdateUseCase {

    fun update(teacherId: UUID, teacherLessonsUpdateRequest: TeacherLessonsUpdateRequest): Either<OperationError, Unit>
}

@Service
class TeacherLessonsUpdateUseCaseImpl(
    private val teacherLessonsUpdateRequestValidator: TeacherLessonsUpdateRequestValidator,
    private val teacherLessonService: TeacherLessonService,
    private val teacherProfileService: TeacherProfileService
) : TeacherLessonsUpdateUseCase {

    @Transactional
    override fun update(
        teacherId: UUID,
        teacherLessonsUpdateRequest: TeacherLessonsUpdateRequest
    ): Either<OperationError, Unit> {
        teacherProfileService.getTeacherById(teacherId) ?: return NotFoundError("TEACHER_NOT_FOUND").left()
        return teacherLessonsUpdateRequestValidator.validate(teacherLessonsUpdateRequest)
            .mapLeft(::ValidationErrors)
            .map {
                teacherLessonService.deleteAllByTeacherId(teacherId)
                teacherLessonService.addAllToTeacher(teacherId, teacherLessonsUpdateRequest.lessons)
            }
    }
}
