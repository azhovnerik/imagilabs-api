package com.anahoret.imagilabsapi.signup.domain

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.projects.domain.usecases.ProjectSamplesCreateUseCase
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import com.anahoret.imagilabsapi.teachers.domain.TeacherSignupRequest
import com.anahoret.imagilabsapi.teachers.export.googlesheets.domain.GoogleSheetsTeachersExportUseCase
import com.anahoret.imagilabsapi.teacherchecklist.domain.CreateTeacherCheckListUseCase
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
    private val googleSheetsTeachersExportUseCase: GoogleSheetsTeachersExportUseCase?,
    private val createTeacherCheckListUseCase: CreateTeacherCheckListUseCase,
    private val projectSamplesCreateUseCase: ProjectSamplesCreateUseCase
) : TeacherSignUpUseCase {

    override fun signUp(request: TeacherSignupRequest): Either<List<ValidationError>, TeacherProfile> {
        val normalizedRequest = request.normalize()
        return teacherSignupRequestValidator.validate(normalizedRequest)
            .map {
                val teacherProfile = teacherProfileService.createTeacher(normalizedRequest)
                createTeacherCheckListUseCase.create(teacherProfile.id)
                projectSamplesCreateUseCase.create(teacherProfile)
                teacherEmailVerificationService.generateNewVerificationCode(teacherProfile.id)
                    ?.let { code -> teacherEmailVerificationCodeSenderUseCase.send(request.email, code) }
                googleSheetsTeachersExportUseCase?.exportAsync(teacherProfile.id)
                teacherProfile
            }
    }

}
