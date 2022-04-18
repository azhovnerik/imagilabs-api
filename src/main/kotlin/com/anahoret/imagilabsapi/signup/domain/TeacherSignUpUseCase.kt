package com.anahoret.imagilabsapi.signup.domain

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import com.anahoret.imagilabsapi.teachers.domain.TeacherSignupRequest
import com.anahoret.imagilabsapi.teachers.export.googlesheets.domain.GoogleSheetsTeachersExportUseCase
import org.springframework.stereotype.Service

interface TeacherSignUpUseCase {

    fun signUp(request: TeacherSignupRequest): Either<List<ValidationError>, TeacherProfile>
}

@Service
class TeacherSignUpUseCaseImpl(
    private val teacherProfileService: TeacherProfileService,
    private val teacherSignupRequestValidator: TeacherSignupRequestValidator,
    private val teacherEmailVerificationService: TeacherEmailVerificationService,
    private val teacherEmailVerificationCodeSenderUseCase: TeacherEmailVerificationCodeSenderUseCase,
    private val googleSheetsTeachersExportUseCase: GoogleSheetsTeachersExportUseCase?
) : TeacherSignUpUseCase {

    override fun signUp(request: TeacherSignupRequest): Either<List<ValidationError>, TeacherProfile> {
        val normalizedRequest = request.normalize()
        return teacherSignupRequestValidator.validate(normalizedRequest)
            .map {
                val teacherProfile = teacherProfileService.createTeacher(normalizedRequest)
                teacherEmailVerificationService.generateNewVerificationCode(teacherProfile.id)
                    ?.let { code -> teacherEmailVerificationCodeSenderUseCase.send(request.email, code) }
                googleSheetsTeachersExportUseCase?.exportAsync(teacherProfile.id)
                teacherProfile
            }
    }

}
