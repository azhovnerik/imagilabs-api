package com.anahoret.imagilabsapi.lovable.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.stereotype.Service

interface ConnectLovableAccountToUserUseCase {
    fun connect(userProfile: UserProfile): Either<LovableConnectionError, LovableAccount>
}

@Service
class ConnectLovableAccountToUserUseCaseImpl(
    private val lovableAccountService: LovableAccountService
) : ConnectLovableAccountToUserUseCase {
    override fun connect(userProfile: UserProfile): Either<LovableConnectionError, LovableAccount> {
        if (userProfile.userType != UserType.STUDENT && userProfile.userType != UserType.TEACHER)
            return UnapplicableUserTypeError().left()

        if (lovableAccountService.connectedCount(userProfile.id) >= Settings.MAX_CONNECTED_ACCOUNTS)
            return MaxNumberOfConnectedAccountsExceededError().left()

        val connectedAccount = lovableAccountService.connectToUser(userProfile)
            ?: return OutOfLovableAccountsError().left()
        return connectedAccount.right()
    }

}
