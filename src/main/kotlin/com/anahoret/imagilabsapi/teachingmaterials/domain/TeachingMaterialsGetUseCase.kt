package com.anahoret.imagilabsapi.teachingmaterials.domain

import arrow.core.Either
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionPlan
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.userclassroomlink.domain.CoTeacherClassroomLinkService
import org.springframework.stereotype.Service
import java.util.UUID

interface TeachingMaterialsGetUseCase {

    fun get(teacherId: UUID): Either<OperationError, TeachingMaterials>
}

@Service
class TeachingMaterialsGetUseCaseImpl(
    private val teacherSubscriptionService: TeacherSubscriptionService,
    private val teacherBundleService: TeacherBundleService,
    private val lessonBundleService: LessonBundleService
): TeachingMaterialsGetUseCase {

    override fun get(teacherId: UUID): Either<OperationError, TeachingMaterials> {
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
