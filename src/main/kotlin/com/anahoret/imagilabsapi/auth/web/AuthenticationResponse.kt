package com.anahoret.imagilabsapi.auth.web

import com.fasterxml.jackson.annotation.JsonInclude
import java.util.*

sealed class AuthenticationResponse

class UserData(
    val id: UUID,
    val userType: String,
    val tokenExp: Long,
    val profile: UserProfileData?
)

interface UserProfileData
class TeacherUserProfileData() : UserProfileData
class StudentUserProfileData() : UserProfileData

@JsonInclude(JsonInclude.Include.NON_NULL)
class AuthenticationSuccess(val currentUser: UserData, val token: String) : AuthenticationResponse()
sealed class AuthenticationError(val error: String) : AuthenticationResponse()
object AuthenticationFailedError : AuthenticationError("AUTHENTICATION_FAILED")
