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
        val includePro = teacherSubscriptionService.getSubscriptionDto(teacherId)
            ?.plan == TeacherSubscriptionPlan.PRO

        val bundleLessons = getBundleLessons(teacherId)
        val worksheets = bundleLessons.map { TeachingMaterial.worksheetFromBundleLesson(it, includePro) }
        val teachingSlides = bundleLessons.map { TeachingMaterial.teachingSlidesFromBundleLesson(it, includePro) }
        return TeachingMaterials(teachingSlides, worksheets)
    }

    private fun getBundleLessons(teacherId: UUID): List<BundleLesson> {
        return teacherBundleService.getBundleLessonsByTeacherId(teacherId) + getDefaultBundle()
    }

    private fun getDefaultBundle(): List<BundleLesson> {
        return lessonBundleService.getDefaultBundle()?.lessons
            ?: emptyList()
    }
}
