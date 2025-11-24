package com.anahoret.imagilabsapi.teachingmaterials.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.error.UnsupportedUserTypeError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import org.springframework.stereotype.Service
import java.time.Clock
import java.util.*

interface TeachingMaterialsGetUseCase {

    fun get(getBy: UserProfile): Either<OperationError, TeachingMaterials>
}

@Service
class TeachingMaterialsGetUseCaseImpl(
    private val teacherBundleService: TeacherBundleService,
    private val lessonBundleService: LessonBundleService,
    private val teacherProfileService: TeacherProfileService,
    private val clock: Clock
) : TeachingMaterialsGetUseCase {

    override fun get(getBy: UserProfile): Either<OperationError, TeachingMaterials> {
        return when (getBy) {
            is TeacherProfile -> getTeachingMaterials(getBy).right()
            is StudentProfile -> getTeachingMaterialsForStudent(getBy).right()
            else -> UnsupportedUserTypeError.left()
        }
    }

    private fun getTeachingMaterials(teacher: TeacherProfile): TeachingMaterials {
        val bundleLessons = getBundleLessons(teacher.id)
        val now = clock.instant().toEpochMilli()
        val proEnabled = teacher.hasProSubscription(now)
        val worksheets = bundleLessons.map { TeachingMaterial.worksheetFromBundleLesson(it, proEnabled) }
        val teachingSlides = bundleLessons.map { TeachingMaterial.teachingSlidesFromBundleLesson(it, proEnabled) }
        return TeachingMaterials(teachingSlides, worksheets)
    }

    private fun getTeachingMaterialsForStudent(studentProfile: StudentProfile): TeachingMaterials {
        val now = clock.instant().toEpochMilli()
        val allTeachers = teacherProfileService.listTeachersByStudent(studentProfile.id)
        val highestSubscriptionTeacher = allTeachers
            .find { it.hasProSubscription(now) }
            ?: allTeachers.firstOrNull()
            ?: return TeachingMaterials.empty()

        val teachingMaterials = getTeachingMaterials(highestSubscriptionTeacher)
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
