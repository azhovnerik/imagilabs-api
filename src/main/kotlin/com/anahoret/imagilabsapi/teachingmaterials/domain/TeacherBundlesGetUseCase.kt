package com.anahoret.imagilabsapi.teachingmaterials.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
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
): TeacherBundlesGetUseCase {

    override fun getAll(teacherId: UUID, currentUser: UserProfile): Either<OperationError, List<LessonBundle>> {

        if (!teacherProfileService.exists(teacherId))
            return NotFoundError("TEACHER_NOT_FOUND").left()

        return when (currentUser.userType) {
            UserType.ADMIN -> teacherBundleService.getBundlesByTeacherId(teacherId).right()
            else -> {
                if (currentUser.id == teacherId) teacherBundleService.getBundlesByTeacherId(teacherId).right()
                else AccessDeniedError("TEACHER_CAN_GET_ONLY_HIS_BUNDLES").left()
            }
        }
    }
}
