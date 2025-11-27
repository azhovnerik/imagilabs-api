package com.anahoret.imagilabsapi.common.domain.profiles

import com.anahoret.imagilabsapi.users.UserType
import java.util.*

interface UserProfile {

    val id: UUID
    val userType: UserType
    val fullName: String
}

object SystemProfile : UserProfile {
    override val id: UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")
    override val userType: UserType = UserType.SYSTEM
    override val fullName: String = "SYSTEM"
}
