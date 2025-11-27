package com.anahoret.imagilabsapi.pythoncompiler.domain

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.stereotype.Service

interface PythonCompilerAccessService {

    fun canRunCode(userProfile: UserProfile): Boolean
}

@Service
class PythonCompilerAccessServiceImpl : PythonCompilerAccessService {

    override fun canRunCode(userProfile: UserProfile): Boolean {
        return when (userProfile.userType) {
            UserType.TEACHER -> true
            UserType.STUDENT -> true
            else -> false
        }
    }
}
