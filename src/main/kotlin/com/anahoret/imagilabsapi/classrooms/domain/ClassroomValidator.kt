package com.anahoret.imagilabsapi.classrooms.domain

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.validation.AbstractValidator
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.userclassroomlink.domain.StudentClassroomLinkService
import org.springframework.stereotype.Service
import java.util.*

interface ClassroomValidator {

    fun validate(teacherProfile: TeacherProfile, request: ClassroomCreateRequest): Either<List<ValidationError>, Unit>
    fun validate(teacherProfile: TeacherProfile, classroomId: UUID, request: ClassroomUpdateRequest): Either<List<ValidationError>, Unit>
}

@Service
class ClassroomValidatorImpl(
    private val studentClassroomLinkService: StudentClassroomLinkService,
    private val teacherSubscriptionService: TeacherSubscriptionService
) : ClassroomValidator, AbstractValidator<ClassroomCreateRequest>() {

    override fun validate(
        teacherProfile: TeacherProfile,
        request: ClassroomCreateRequest
    ): Either<List<ValidationError>, Unit> {
        return validate { errors ->
            with(request) {
                validateName(name, errors)
                validateStudentsCount(teacherProfile, studentCreateRequests.size.toLong(), errors)
            }
        }
    }

    override fun validate(
        teacherProfile: TeacherProfile,
        classroomId: UUID,
        request: ClassroomUpdateRequest
    ): Either<List<ValidationError>, Unit> {
        return validate { errors ->
            with(request) {
                validateName(name, errors)
                val existingStudentCount = studentClassroomLinkService.getStudentCount(classroomId)
                val newStudentCount = studentCreateRequests.size
                validateStudentsCount(teacherProfile, existingStudentCount + newStudentCount, errors)
            }
        }
    }

    fun validateName(name: String, errors: MutableList<ValidationError>) {
        rejectIfBlank(name, errors, "NAME")
        rejectIfTooLong(name, 50, errors, "NAME")
    }

    fun validateStudentsCount(teacherProfile: TeacherProfile, count: Long, errors: MutableList<ValidationError>) {
        if (teacherSubscriptionService.studentLimitPerClassExceeded(teacherProfile, count)) {
            errors.add(ValidationError("TOO_MANY_STUDENTS"))
        }
    }

}
