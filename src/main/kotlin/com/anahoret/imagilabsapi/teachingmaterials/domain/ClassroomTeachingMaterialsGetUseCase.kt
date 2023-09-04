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

                getTeachingMaterials(classroom).right()
            }

            UserType.STUDENT -> {
                if (!classroomAccessService.canGetTeachingMaterials(getBy, classroom))
                    return AccessDeniedError("ACCESS_TO_CLASSROOM_DENIED").left()

                getTeachingMaterialsForStudent(classroom).right()
            }

            else -> AccessDeniedError("ACCESS_TO_CLASSROOM_DENIED").left()
        }
    }

    private fun getTeachingMaterials(classroom: Classroom): TeachingMaterials {
        val bundleLessons = getBundleLessons(classroom.teacherId)

        val proEnabled = teacherSubscriptionService.getSubscriptionDto(classroom.teacherId)
            ?.plan == TeacherSubscriptionPlan.PRO

        val worksheets = bundleLessons.map { TeachingMaterial.worksheetFromBundleLesson(it, proEnabled) }
        val teachingSlides = bundleLessons.map { TeachingMaterial.teachingSlidesFromBundleLesson(it, proEnabled) }
        return TeachingMaterials(teachingSlides, worksheets)
    }

    private fun getTeachingMaterialsForStudent(classroom: Classroom): TeachingMaterials {
        val teachingMaterials = getTeachingMaterials(classroom)
        return TeachingMaterials(
            teachingMaterials.teachingSlides.filter { it.path != null },
            teachingMaterials.worksheets.filter { it.path != null }
        )
    }

    private fun getBundleLessons(teacherId: UUID): List<BundleLesson> {
        return teacherBundleService.getBundleLessonsByTeacherId(teacherId) + getDefaultBundle()
    }

    private fun getDefaultBundle(): List<BundleLesson> {
        return lessonBundleService.getDefaultBundle()?.lessons
            ?: emptyList()
    }

}
