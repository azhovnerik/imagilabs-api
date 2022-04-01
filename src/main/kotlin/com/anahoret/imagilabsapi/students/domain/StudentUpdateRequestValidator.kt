package com.anahoret.imagilabsapi.students.domain

import arrow.core.Either
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.domain.validation.AbstractValidator
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.userclassroomlink.domain.StudentClassroomLink
import com.anahoret.imagilabsapi.userclassroomlink.domain.StudentClassroomLinkService
import org.springframework.stereotype.Service
import java.util.*

interface StudentUpdateRequestValidator {

    fun validate(
        studentId: UUID,
        currentCredentials: StudentCredentials,
        studentUpdateRequest: StudentUpdateRequest
    ): Either<List<ValidationError>, Unit>

}

@Service
class StudentUpdateRequestValidatorImpl(
    private val studentClassroomLinkService: StudentClassroomLinkService,
    private val studentProfileService: StudentProfileService,
    private val classroomService: ClassroomService
) : StudentUpdateRequestValidator, AbstractValidator<StudentUpdateRequest>() {

    override fun validate(
        studentId: UUID,
        currentCredentials: StudentCredentials,
        studentUpdateRequest: StudentUpdateRequest
    ): Either<List<ValidationError>, Unit> {
        return validate { errors ->
            rejectIfBlank(studentUpdateRequest.name, errors, "NAME")
            rejectIfBlank(studentUpdateRequest.username, errors, "USERNAME")
            if (currentCredentials.username != studentUpdateRequest.username) {
                validateCredentialsConflict(errors, studentId, currentCredentials, studentUpdateRequest)
            }
        }
    }

    private fun validateCredentialsConflict(
        errors: MutableList<ValidationError>,
        studentId: UUID,
        currentCredentials: StudentCredentials,
        studentUpdateRequest: StudentUpdateRequest
    ) {
        val newCredentialsExists = studentClassroomLinkService.getLinks(studentId)
            .map(StudentClassroomLink::classroomId)
            .let(classroomService::listByIds)
            .map {
                StudentClassroomCredentials(
                    id = studentId,
                    username = studentUpdateRequest.username,
                    classroomAccessCode = it.accessCode,
                    password = currentCredentials.password,
                )
            }.any { studentProfileService.studentCredentialsExists(it) }

        if (newCredentialsExists) errors.add(ValidationError("STUDENT_WITH_SAME_CREDENTIALS_EXISTS"))
    }

}
