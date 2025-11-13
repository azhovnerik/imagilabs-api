package com.anahoret.imagilabsapi.students.domain

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.validation.AbstractValidator
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.userclassroomlink.domain.StudentClassroomLinkService
import org.springframework.stereotype.Service

interface StudentUpdateRequestValidator {

    fun validate(
        studentProfile: StudentProfile,
        currentCredentials: StudentCredentials,
        studentUpdateRequest: StudentUpdateRequest
    ): Either<List<ValidationError>, Unit>

}

@Service
class StudentUpdateRequestValidatorImpl(
    private val studentProfileService: StudentProfileService,
    private val studentClassroomLinkService: StudentClassroomLinkService
) : StudentUpdateRequestValidator, AbstractValidator<StudentUpdateRequest>() {

    override fun validate(
        studentProfile: StudentProfile,
        currentCredentials: StudentCredentials,
        studentUpdateRequest: StudentUpdateRequest
    ): Either<List<ValidationError>, Unit> {
        return validate { errors ->
            rejectIfBlank(studentUpdateRequest.name, errors, "NAME")
            rejectIfBlank(studentUpdateRequest.username, errors, "USERNAME")
            if (currentCredentials.username != studentUpdateRequest.username) {
                validateCredentialsConflict(errors, studentProfile, currentCredentials, studentUpdateRequest)
            }
        }
    }

    private fun validateCredentialsConflict(
        errors: MutableList<ValidationError>,
        studentProfile: StudentProfile,
        currentCredentials: StudentCredentials,
        studentUpdateRequest: StudentUpdateRequest
    ) {
        val classrooms = studentClassroomLinkService.listClassroomsByStudent(studentProfile.id)
        if (classrooms.isEmpty()) {
            errors.add(ValidationError("STUDENT_CLASSROOM_DOES_NOT_EXIST"))
            return
        }

        val newCredentialsExists = classrooms.any { classroom ->
            val newCredentials = StudentClassroomCredentials(
                id = studentProfile.id,
                username = studentUpdateRequest.username,
                classroomAccessCode = classroom.accessCode,
                password = currentCredentials.password,
            )
            studentProfileService.studentCredentialsExists(newCredentials)
        }

        if (newCredentialsExists) errors.add(ValidationError("STUDENT_WITH_SAME_CREDENTIALS_EXISTS"))
    }

}
