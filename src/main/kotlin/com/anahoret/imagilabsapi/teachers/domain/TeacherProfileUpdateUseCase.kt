package com.anahoret.imagilabsapi.teachers.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

interface TeacherProfileUpdateUseCase {
    fun update(teacherId: UUID, request: TeacherProfileUpdateRequest): Either<OperationError, TeacherProfile>
}

@Service
class TeacherProfileUpdateUseCaseImpl(
    private val teacherProfileService: TeacherProfileService
) : TeacherProfileUpdateUseCase {

    @Transactional
    override fun update(teacherId: UUID, request: TeacherProfileUpdateRequest): Either<OperationError, TeacherProfile> {
        return teacherProfileService.updateProfile(teacherId, request)?.right()
            ?: NotFoundError("TEACHER_NOT_FOUND").left()
    }
}
