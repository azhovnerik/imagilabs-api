package com.anahoret.imagilabsapi.auth.web

import com.anahoret.imagilabsapi.auth.web.jwt.JwtTokenData
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.fasterxml.jackson.annotation.JsonInclude
import java.util.*

sealed class AuthenticationResponse

class UserData(
    val id: UUID,
    val userType: String,
    val profile: UserProfileData?
)

interface UserProfileData
class TeacherUserProfileData(val teacherProfile: TeacherProfile) : UserProfileData
class StudentUserProfileData() : UserProfileData

@JsonInclude(JsonInclude.Include.NON_NULL)
class AuthenticationSuccess(val currentUser: UserData, val jwtToken: JwtTokenData) : AuthenticationResponse()
sealed class AuthenticationError(val error: String) : AuthenticationResponse()
object AuthenticationFailedError : AuthenticationError("AUTHENTICATION_FAILED")
