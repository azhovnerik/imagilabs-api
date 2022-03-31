package com.anahoret.imagilabsapi.teachingmaterials.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import org.springframework.stereotype.Service
import java.util.*

interface LessonBundleAddToTeacherUseCase {

    fun add(bundleId: UUID, teacherId: UUID): Either<OperationError, Unit>
}

@Service
class LessonBundleAddToTeacherUseCaseImpl(
    private val teacherProfileService: TeacherProfileService,
    private val lessonBundleService: LessonBundleService,
    private val teacherLessonService: TeacherLessonService
) : LessonBundleAddToTeacherUseCase {

    override fun add(bundleId: UUID, teacherId: UUID): Either<OperationError, Unit> {
        if (!teacherProfileService.exists(teacherId)) return NotFoundError("TEACHER_NOT_FOUND").left()
        val bundle = lessonBundleService.get(bundleId) ?: return NotFoundError("LESSON_BUNDLE_NOT_FOUND").left()

        val lessonsData = bundle.lessons.map { LessonData.fromBundleLesson(it) }
        teacherLessonService.addAllToTeacher(teacherId, lessonsData)
        return Unit.right()
    }
}
