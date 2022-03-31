@file:Suppress("unused")

package com.anahoret.imagilabsapi.auth.web

import com.anahoret.imagilabsapi.admins.domain.AdminProfile
import com.anahoret.imagilabsapi.auth.web.jwt.JwtTokenData
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.fasterxml.jackson.annotation.JsonInclude
import java.util.*

sealed class UserData(
    val id: UUID,
    val userType: String,
)

class TeacherUserData(
    id: UUID,
    userType: String,
    val profile: TeacherUserProfileData?
) : UserData(id, userType)

class StudentUserData(
    id: UUID,
    userType: String,
    val profile: StudentUserProfileData?,
    val currentClassroomId: UUID
) : UserData(id, userType)

class AdminUserData(
    id: UUID,
    userType: String,
    val profile: AdminUserProfileData?
) : UserData(id, userType)

sealed interface UserProfileData
class TeacherUserProfileData(val teacherProfile: TeacherProfile) : UserProfileData
class StudentUserProfileData(val studentProfile: StudentProfile) : UserProfileData
class AdminUserProfileData(val adminProfile: AdminProfile) : UserProfileData

sealed class AuthenticationSuccess(val jwtToken: JwtTokenData)

@JsonInclude(JsonInclude.Include.NON_NULL)
class TeacherAuthenticationSuccess(val currentUser: TeacherUserData, jwtToken: JwtTokenData) :
    AuthenticationSuccess(jwtToken)

@JsonInclude(JsonInclude.Include.NON_NULL)
class StudentAuthenticationSuccess(val currentUser: StudentUserData, jwtToken: JwtTokenData) :
    AuthenticationSuccess(jwtToken)

@JsonInclude(JsonInclude.Include.NON_NULL)
class AdminAuthenticationSuccess(val currentUser: AdminUserData, jwtToken: JwtTokenData) :
    AuthenticationSuccess(jwtToken)
