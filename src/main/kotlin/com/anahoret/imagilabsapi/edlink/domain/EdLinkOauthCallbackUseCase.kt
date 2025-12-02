package com.anahoret.imagilabsapi.edlink.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.edlink.api.EdLinkDistrictApi
import com.anahoret.imagilabsapi.edlink.api.EdLinkIntegrationApi
import com.anahoret.imagilabsapi.edlink.api.EdLinkPersonApi
import com.anahoret.imagilabsapi.edlink.api.EdLinkProfileApi
import com.anahoret.imagilabsapi.edlink.api.EdLinkSchoolApi
import com.anahoret.imagilabsapi.edlink.api.EdLinkTokenApi
import com.anahoret.imagilabsapi.edlink.api.model.Person
import com.anahoret.imagilabsapi.signup.domain.TeacherSignUpUseCase
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import com.anahoret.imagilabsapi.teachers.domain.TeacherSignupRequest
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.stereotype.Service
import java.util.*

interface EdLinkOauthCallbackUseCase {
    fun tryAuthenticate(
        request: EdLinkOAuthCallbackRequest,
        isLocalhostRedirect: Boolean
    ): Either<OperationError, UserProfile>
}

@Service
class EdLinkOauthCallbackUseCaseImpl(
    private val edLinkOAuthStateService: EdLinkOAuthStateService,
    private val edLinkProfileApi: EdLinkProfileApi,
    private val edLinkTokenApi: EdLinkTokenApi,
    private val edLinkIntegrationApi: EdLinkIntegrationApi,
    private val edLinkDistrictApi: EdLinkDistrictApi,
    private val edLinkPersonApi: EdLinkPersonApi,
    private val edLinkSchoolApi: EdLinkSchoolApi,
    private val edLinkEnumMapper: EdLinkEnumMapper,
    private val edLinkSubjectsService: EdLinkSubjectsService,
    private val studentProfileService: StudentProfileService,
    private val teacherProfileService: TeacherProfileService,
    private val teacherSignUpUseCase: TeacherSignUpUseCase,
    private val edLinkRefreshTeacherClassesUseCase: EdLinkRefreshTeacherClassesUseCase
) : EdLinkOauthCallbackUseCase {

    override fun tryAuthenticate(
        request: EdLinkOAuthCallbackRequest,
        isLocalhostRedirect: Boolean
    ): Either<OperationError, UserProfile> {
        if (!edLinkOAuthStateService.exists(request.state)) {
            return AccessDeniedError("INVALID_STATE").left()
        }
        edLinkOAuthStateService.delete(request.state)
        return either {
            val token = edLinkTokenApi.exchange(request.code, isLocalhostRedirect).bind()
            val profile = edLinkProfileApi.myProfile(token).bind()
            val integration = edLinkIntegrationApi.myIntegration(token).bind()
            val userType = profile.getUserType().bind()
            when (userType) {
                UserType.TEACHER -> authenticateTeacher(integration.id, profile, request.mobileAppClient, token).bind()
                UserType.STUDENT -> authenticateStudent(integration.id, profile).bind()
                else -> AccessDeniedError("UNSUPPORTED_USER_TYPE").left().bind()
            }
        }

    }

    private fun authenticateTeacher(
        integrationId: UUID,
        person: Person,
        mobileAppClient: Boolean,
        token: String
    ): Either<OperationError, TeacherProfile> {
        val existingTeacher = teacherProfileService.getByEdLink(integrationId, person.id)
            ?: teacherProfileService.getTeacherByEmail(person.email)
                ?.also { teacherProfileService.setEdLinkId(it.id, integrationId, person.id) }
        if (existingTeacher != null) return existingTeacher.right()
        return either {
            val district = edLinkDistrictApi.myDistrict(token, person.districtId).bind()

            // Get additional person details from graph API (state)
            val personDetails = edLinkPersonApi.getPerson(token, person.id).bind()

            // Fetch school names for each school ID
            val schoolNames = person.schools.mapNotNull { schoolId ->
                edLinkSchoolApi.getSchool(token, schoolId)
                    .fold(
                        { null }, // Ignore errors for individual schools
                        { school -> school.name }
                    )
            }.joinToString(", ")

            // Map EdLink API strings to domain enums
            val gradeLevels = edLinkEnumMapper.mapGradeLevels(person.gradeLevels)
            val schoolRoles = edLinkEnumMapper.mapSchoolRoles(person.roles)

            // Fetch teacher's subjects from enrollments -> classes -> subjects
            val subjects = edLinkSubjectsService.getTeacherSubjects(token, person.id)
                .fold(
                    { it },
                    { it }
                )

            val newTeacher = teacherSignUpUseCase.signUp(
                TeacherSignupRequest(
                    person.email,
                    password = "",
                    firstName = person.firstName,
                    lastName = person.lastName,
                    country = person.address.country ?: "",
                    organization = district.name,
                    howDidYouHearAboutUs = "",
                    howDidYouHearAboutUsOther = null,
                    marketingEmailSubscribed = false,
                    mobileAppClient = mobileAppClient,
                    edLinkIntegrationId = integrationId,
                    edLinkPersonId = person.id,
                    state = personDetails.state,
                    schoolRoles = schoolRoles,
                    grades = gradeLevels,
                    subjects = subjects,
                    schools = schoolNames.ifEmpty { null }
                )
            ).bind()
            edLinkRefreshTeacherClassesUseCase.refresh(newTeacher).bind()
            newTeacher
        }.mapLeft { left ->
            when (left) {
                is OperationError -> left
                else -> AccessDeniedError("TEACHER_CREATION_FAILED") // Should never happen as the request validation is skipped for EdLink signup requests
            }
        }
    }

    private fun authenticateStudent(integrationId: UUID, person: Person): Either<OperationError, StudentProfile> {
        return studentProfileService.getByEdLink(integrationId, person.id)
            ?.right()
            ?: AccessDeniedError("STUDENT_NOT_FOUND").left()
    }

}
