package com.anahoret.imagilabsapi.common.domain.profiles

import com.anahoret.imagilabsapi.users.UserType
import java.util.*

interface UserProfile {

    val id: UUID
    val userType: UserType
}
