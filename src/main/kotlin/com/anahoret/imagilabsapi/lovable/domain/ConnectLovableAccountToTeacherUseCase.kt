package com.anahoret.imagilabsapi.lovable.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.stereotype.Service

interface ConnectLovableAccountToTeacherUseCase {
    fun connect(teacherProfile: TeacherProfile): Either<LovableConnectionError, LovableAccount>
}

@Service
class ConnectLovableAccountToTeacherUseCaseImpl(
    private val lovableAccountService: LovableAccountService
) : ConnectLovableAccountToTeacherUseCase {
    override fun connect(teacherProfile: TeacherProfile): Either<LovableConnectionError, LovableAccount> {
        if (lovableAccountService.connectedCount(teacherProfile.id) >= Settings.MAX_CONNECTED_ACCOUNTS) {
            return MaxNumberOfConnectedAccountsExceededError().left()
        }
        val connectedAccount = lovableAccountService.connectToUser(teacherProfile)
            ?: return OutOfLovableAccountsError().left()
        return connectedAccount.right()
    }

}
