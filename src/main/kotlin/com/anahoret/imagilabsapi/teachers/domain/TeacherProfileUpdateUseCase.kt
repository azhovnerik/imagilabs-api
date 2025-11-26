package com.anahoret.imagilabsapi.teachers.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.schools.domain.SchoolService
import com.anahoret.imagilabsapi.schools.storage.SchoolRepository
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
    private val teacherProfileService: TeacherProfileService,
    private val schoolService: SchoolService,
    private val schoolRepository: SchoolRepository
) : TeacherProfileUpdateUseCase {

    @Transactional
    override fun update(teacherId: UUID, request: TeacherProfileUpdateRequest): Either<OperationError, TeacherProfile> {
        val teacherEntity = teacherProfileEntityRepository.findByIdOrNull(teacherId)
            ?: return NotFoundError("TEACHER_NOT_FOUND").left()

        // Validate school IDs if provided
        if (request.schoolIds != null) {
            val invalidSchoolIds = request.schoolIds.filter { schoolId ->
                !schoolRepository.existsById(schoolId)
            }
            if (invalidSchoolIds.isNotEmpty()) {
                return ValidationError("INVALID_SCHOOL_IDS").left()
            }
        }

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
        request.subjects?.let {
            teacherEntity.subjects = TeacherProfile.subjectsToString(it)
        }

        // Update schools (full replacement)
        request.schoolIds?.let { schoolIds ->
            val schools = schoolRepository.findAllById(schoolIds)
            teacherEntity.schools.clear()
            teacherEntity.schools.addAll(schools)
        }

        teacherProfileEntityRepository.save(teacherEntity)

        return teacherProfileService.getTeacherById(teacherId)?.right()
            ?: NotFoundError("TEACHER_NOT_FOUND").left()
    }
}