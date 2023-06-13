package com.anahoret.imagilabsapi.teachingmaterials.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.classrooms.domain.Classroom
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

interface ClassroomTeachingMaterialsGetUseCase {

    fun get(getBy: UserProfile, classroomId: UUID): Either<OperationError, TeachingMaterials>
}

@Service
class ClassroomTeachingMaterialsGetUseCaseImpl(
    private val teacherBundleService: TeacherBundleService,
    private val classroomService: ClassroomService,
    private val classroomAccessService: ClassroomAccessService,
    private val lessonBundleService: LessonBundleService,
    private val teacherSubscriptionService: TeacherSubscriptionService
) : ClassroomTeachingMaterialsGetUseCase {

    override fun get(getBy: UserProfile, classroomId: UUID): Either<OperationError, TeachingMaterials> {
        val classroom = classroomId.let(classroomService::getById)
            ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()

        return when (getBy.userType) {
            UserType.TEACHER -> {
                if (!classroomAccessService.canGetTeachingMaterials(getBy, classroom))
                    return AccessDeniedError("ACCESS_TO_CLASSROOM_DENIED").left()

                getForTeacher(classroom)
            }

            UserType.STUDENT -> getForStudent(getBy, classroom)
            else -> AccessDeniedError("ACCESS_TO_CLASSROOM_DENIED").left()
        }
    }

    private fun getForStudent(
        userProfile: UserProfile,
        classroom: Classroom
    ): Either<OperationError, TeachingMaterials> {

        if (!classroomAccessService.canGetTeachingMaterials(userProfile, classroom))
            return AccessDeniedError("ACCESS_TO_CLASSROOM_DENIED").left()

        return getForTeacher(classroom)
    }

    private fun getForTeacher(classroom: Classroom): Either<OperationError, TeachingMaterials> {
        val bundleLessons = getBundleLessons(classroom.teacherId)

        val includePro = teacherSubscriptionService.getSubscriptionDto(classroom.teacherId)
            ?.plan == TeacherSubscriptionPlan.PRO

        val worksheets = bundleLessons.map { TeachingMaterial.worksheetFromBundleLesson(it, includePro) }
        val teachingSlides = bundleLessons.map { TeachingMaterial.teachingSlidesFromBundleLesson(it, includePro) }
        return TeachingMaterials(teachingSlides, worksheets).right()
    }

    private fun getBundleLessons(teacherId: UUID): List<BundleLesson> {
        return teacherBundleService.getBundleLessonsByTeacherId(teacherId) + getDefaultBundle()
    }

    private fun getDefaultBundle(): List<BundleLesson> {
        return lessonBundleService.getDefaultBundle()?.lessons
            ?: emptyList()
    }

}
