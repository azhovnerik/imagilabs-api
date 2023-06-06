package com.anahoret.imagilabsapi.teachingmaterials.domain

import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionPlan
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionService
import org.springframework.stereotype.Service
import java.util.*

interface TeachingMaterialsGetUseCase {

    fun get(teacherId: UUID): TeachingMaterials?
}

@Service
class TeachingMaterialsGetUseCaseImpl(
    private val teacherSubscriptionService: TeacherSubscriptionService,
    private val teacherBundleService: TeacherBundleService,
    private val lessonBundleService: LessonBundleService
) : TeachingMaterialsGetUseCase {

    override fun get(teacherId: UUID): TeachingMaterials? {
        val bundleLessons = getBundleLessons(teacherId)
        val worksheets = bundleLessons.map(TeachingMaterial.Companion::worksheetFromBundleLesson)
        val teachingSlides = bundleLessons.map(TeachingMaterial.Companion::teachingSlidesFromBundleLesson)
        return TeachingMaterials(teachingSlides, worksheets)
    }

    private fun getBundleLessons(teacherId: UUID): List<BundleLesson> {
        val includePro = teacherSubscriptionService.getSubscriptionDto(teacherId)
            ?.plan == TeacherSubscriptionPlan.PRO
        return teacherBundleService.getBundleLessonsByTeacherId(teacherId, includePro) + getDefaultBundle(includePro)
    }

    private fun getDefaultBundle(includePro: Boolean): List<BundleLesson> {
        return lessonBundleService.getDefaultBundle(includePro)?.lessons
            ?: emptyList()
    }
}
