package com.anahoret.imagilabsapi.lovable.domain

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import org.springframework.stereotype.Service

interface GetLovableAccountForUserUseCase {
    fun get(userProfile: UserProfile): LovableAccount?
}

@Service
class GetLovableAccountForUserUseCaseImpl(
    private val lovableAccountService: LovableAccountService
) : GetLovableAccountForUserUseCase {
    override fun get(userProfile: UserProfile): LovableAccount? {
        return lovableAccountService.getActive(userProfile)
    }

}
