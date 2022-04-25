package com.anahoret.imagilabsapi.classrooms.domain

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.validation.AbstractValidator
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.userclassroomlink.domain.StudentClassroomLinkService
import org.springframework.stereotype.Service
import java.util.*

interface ClassroomValidator {

    fun validate(request: ClassroomCreateRequest): Either<List<ValidationError>, Unit>
    fun validate(classroomId: UUID, request: ClassroomUpdateRequest): Either<List<ValidationError>, Unit>
}

@Service
class ClassroomValidatorImpl(
    private val studentClassroomLinkService: StudentClassroomLinkService
) : ClassroomValidator, AbstractValidator<ClassroomCreateRequest>() {

    companion object {

        const val MAX_STUDENTS_PER_CLASSROOM = 200
    }

    override fun validate(request: ClassroomCreateRequest): Either<List<ValidationError>, Unit> {
        return validate { errors ->
            with(request) {
                validateName(name, errors)
                validateStudentsCount(studentCreateRequests.size.toLong(), errors)
            }
        }
    }

    override fun validate(classroomId: UUID, request: ClassroomUpdateRequest): Either<List<ValidationError>, Unit> {
        return validate { errors ->
            with(request) {
                validateName(name, errors)
                val existingStudentCount = studentClassroomLinkService.getStudentCount(classroomId)
                val newStudentCount = studentCreateRequests.size
                validateStudentsCount(existingStudentCount + newStudentCount, errors)
            }
        }
    }

    fun validateName(name: String, errors: MutableList<ValidationError>) {
        rejectIfBlank(name, errors, "NAME")
        rejectIfTooLong(name, 50, errors, "NAME")
    }

    fun validateStudentsCount(count: Long, errors: MutableList<ValidationError>) {
        if (count > MAX_STUDENTS_PER_CLASSROOM) {
            errors.add(ValidationError("TOO_MANY_STUDENTS"))
        }
    }

}
