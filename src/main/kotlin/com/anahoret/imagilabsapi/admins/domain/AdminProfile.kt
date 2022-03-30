package com.anahoret.imagilabsapi.admins.domain

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.users.UserType
import java.util.*

class AdminProfile(
    override val id: UUID,
    override val fullName: String
) : UserProfile {

    override val userType = UserType.ADMIN
}
