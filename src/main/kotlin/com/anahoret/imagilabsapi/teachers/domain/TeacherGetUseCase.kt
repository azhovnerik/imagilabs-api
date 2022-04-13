package com.anahoret.imagilabsapi.teachers.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import org.springframework.stereotype.Service
import java.util.*

interface TeacherGetUseCase {

    fun get(teacherId: UUID): Either<OperationError, TeacherProfile>
}

@Service
class TeacherGetUseCaseImpl(
    private val teacherProfileService: TeacherProfileService
) : TeacherGetUseCase {

    override fun get(teacherId: UUID): Either<OperationError, TeacherProfile> {
        return teacherProfileService.getTeacherById(teacherId)?.right()
            ?: NotFoundError("TEACHER_NOT_FOUND").left()
    }
}
