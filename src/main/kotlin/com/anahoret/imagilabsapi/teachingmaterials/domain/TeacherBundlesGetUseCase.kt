package com.anahoret.imagilabsapi.teachingmaterials.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionPlan
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.stereotype.Service
import java.util.UUID

interface TeacherBundlesGetUseCase {

    fun getAll(teacherId: UUID, currentUser: UserProfile): Either<OperationError, List<LessonBundle>>
}

@Service
class TeacherBundlesGetUseCaseImpl(
    private val teacherBundleService: TeacherBundleService,
    private val teacherProfileService: TeacherProfileService,
    private val lessonBundleService: LessonBundleService
): TeacherBundlesGetUseCase {

    override fun getAll(teacherId: UUID, currentUser: UserProfile): Either<OperationError, List<LessonBundle>> {

        val teacherProfile = teacherProfileService.getTeacherById(teacherId)
            ?: return NotFoundError("TEACHER_NOT_FOUND").left()

        val includePro = teacherProfile.subscription.plan == TeacherSubscriptionPlan.PRO

        return when (currentUser.userType) {
            UserType.ADMIN -> getTeacherBundles(teacherId).right()
            else -> {
                if (currentUser.id == teacherId) getTeacherBundles(teacherId, includePro).right()
                else AccessDeniedError("TEACHER_CAN_GET_ONLY_HIS_BUNDLES").left()
            }
        }
    }

    private fun getTeacherBundles(teacherId: UUID, includePro: Boolean = true): List<LessonBundle> {
        return teacherBundleService.getBundlesByTeacherId(teacherId, includePro).takeIf { it.isNotEmpty() }
            ?: defaultBundles(includePro)
    }

    private fun defaultBundles(includePro: Boolean): List<LessonBundle> {
        val defaultBundle = lessonBundleService.getDefaultBundle(includePro)
            ?: return emptyList()

        return listOf(defaultBundle)
    }
}
