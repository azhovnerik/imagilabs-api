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
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionPlan
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionService
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.stereotype.Service
import java.util.*

interface TeachingMaterialsGetUseCase {

    fun get(getBy: UserProfile, classroomId: UUID?): Either<OperationError, TeachingMaterials>
}

@Service
class TeachingMaterialsGetUseCaseImpl(
    private val teacherBundleService: TeacherBundleService,
    private val classroomService: ClassroomService,
    private val classroomAccessService: ClassroomAccessService,
    private val lessonBundleService: LessonBundleService,
    private val teacherSubscriptionService: TeacherSubscriptionService
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
        val bundleLessons = getBundleLessons(teacherId)
        val worksheets = bundleLessons.map(TeachingMaterial.Companion::worksheetFromBundleLesson)
        val teachingSlides = bundleLessons.map(TeachingMaterial.Companion::teachingSlidesFromBundleLesson)
        return TeachingMaterials(teachingSlides, worksheets).right()
    }

    private fun getBundleLessons(teacherId: UUID): List<BundleLesson> {
        val includePro = teacherSubscriptionService.getSubscriptionDto(teacherId)
            ?.plan == TeacherSubscriptionPlan.PRO
        return teacherBundleService.getBundleLessonsByTeacherId(teacherId, includePro).takeIf { it.isNotEmpty() }
            ?: lessonBundleService.getDefaultBundle(includePro)?.lessons
            ?: emptyList()
    }

}
