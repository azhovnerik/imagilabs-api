package com.anahoret.imagilabsapi.teachingmaterials.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomAccessService
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.stereotype.Service
import java.util.*

interface TeachingMaterialsGetUseCase {

    fun get(getBy: UserProfile, classroomId: UUID?): Either<OperationError, TeachingMaterials>
}

@Service
class TeachingMaterialsGetUseCaseImpl(
    private val teacherLessonService: TeacherLessonService,
    private val classroomService: ClassroomService,
    private val classroomAccessService: ClassroomAccessService,
    private val lessonBundleService: LessonBundleService
) : TeachingMaterialsGetUseCase {

    override fun get(getBy: UserProfile, classroomId: UUID?): Either<OperationError, TeachingMaterials> {
        return when (getBy.userType) {
            UserType.TEACHER -> getForTeacher(getBy.id)
            UserType.STUDENT -> getForStudent(getBy, classroomId)
            else -> AccessDeniedError("ACCESS_TO_CLASSROOM_DENIED").left()
        }
    }

    private fun getForStudent(userProfile: UserProfile, classroomId: UUID?): Either<OperationError, TeachingMaterials> {
        val classroom = classroomId?.let(classroomService::getById)
            ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()
        if (!classroomAccessService.canGetTeachingMaterials(userProfile, classroom))
            return AccessDeniedError("ACCESS_TO_CLASSROOM_DENIED").left()
        return getForTeacher(classroom.teacherId)
    }

    private fun getForTeacher(teacherId: UUID): Either<OperationError, TeachingMaterials> {
        val teacherLessons = getTeacherLessons(teacherId)
        val worksheets = teacherLessons.map(TeachingMaterial.Companion::worksheetFromTeacherLesson)
        val teachingSlides = teacherLessons.map(TeachingMaterial.Companion::teachingSlidesFromTeacherLesson)
        return TeachingMaterials(teachingSlides, worksheets).right()
    }

    private fun getTeacherLessons(teacherId: UUID): List<TeacherLesson> {
        return teacherLessonService.listByTeacherId(teacherId).takeIf { it.isNotEmpty() }
            ?: lessonBundleService.getDefaultBundle()?.lessons?.map { TeacherLesson.fromBundleLesson(it, teacherId) }
            ?: emptyList()
    }

}
