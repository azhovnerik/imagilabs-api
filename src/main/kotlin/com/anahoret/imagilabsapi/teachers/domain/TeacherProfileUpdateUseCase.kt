package com.anahoret.imagilabsapi.teachers.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntityRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

interface TeacherProfileUpdateUseCase {
    fun update(teacherId: UUID, request: TeacherProfileUpdateRequest): Either<OperationError, TeacherProfile>
}

@Service
class TeacherProfileUpdateUseCaseImpl(
    private val teacherProfileEntityRepository: TeacherProfileEntityRepository,
    private val teacherProfileService: TeacherProfileService
) : TeacherProfileUpdateUseCase {

    @Transactional
    override fun update(teacherId: UUID, request: TeacherProfileUpdateRequest): Either<OperationError, TeacherProfile> {
        val teacherEntity = teacherProfileEntityRepository.findByIdOrNull(teacherId)
            ?: return NotFoundError("TEACHER_NOT_FOUND").left()

        // Update simple fields (only if not null)
        request.firstName?.let { teacherEntity.firstName = it }
        request.lastName?.let { teacherEntity.lastName = it }
        request.country?.let { teacherEntity.country = it }
        request.state?.let { teacherEntity.state = it }
        request.organization?.let { teacherEntity.organization = it }
        request.marketingEmailSubscribed?.let { teacherEntity.marketingEmailSubscribed = it }

        // Update enum list fields (convert to comma-separated strings)
        request.schoolRoles?.let {
            teacherEntity.schoolRoles = TeacherProfile.schoolRolesToString(it)
        }
        request.grades?.let {
            teacherEntity.grades = TeacherProfile.gradesToString(it)
        }

        // Update text fields
        request.subjects?.let { teacherEntity.subjects = it }
        request.schools?.let { teacherEntity.schools = it }

        teacherProfileEntityRepository.save(teacherEntity)

        return teacherProfileService.getTeacherById(teacherId)?.right()
            ?: NotFoundError("TEACHER_NOT_FOUND").left()
    }
}