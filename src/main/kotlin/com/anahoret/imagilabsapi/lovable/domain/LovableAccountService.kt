package com.anahoret.imagilabsapi.lovable.domain

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.lovable.storage.LovableAccountRepository
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.util.*

interface LovableAccountService {
    fun getActive(user: UserProfile): LovableAccount?
    fun connectToUser(user: UserProfile): LovableAccount?
    fun connectedCount(userId: UUID): Long
}

@Service
class LovableAccountServiceImpl(
    private val lovableAccountRepository: LovableAccountRepository,
    private val clock: Clock
) : LovableAccountService {

    @Transactional
    override fun connectToUser(user: UserProfile): LovableAccount? {
        return when (user.userType) {
            UserType.TEACHER, UserType.STUDENT -> doConnect(user.id)
            UserType.ADMIN -> throw IllegalArgumentException("Cannot connect admin")
        }
    }

    override fun connectedCount(userId: UUID): Long {
        return lovableAccountRepository.countByConnectedUser(userId)
    }

    override fun getActive(user: UserProfile): LovableAccount? {
        return lovableAccountRepository.findOneByConnectedUserAndActiveTrue(user.id)
            ?.let { LovableAccount(it.email, it.password) }
    }

    private fun doConnect(id: UUID): LovableAccount? {
        return lovableAccountRepository.findFirstByConnectedUserIsNull()
            ?.let {
                lovableAccountRepository.findByConnectedUser(id)
                    .onEach { it.active = false }
                    .let(lovableAccountRepository::saveAll)

                it.connectedUser = id
                it.connectedAt = clock.millis()
                it.active = true
                lovableAccountRepository.save(it)
                LovableAccount(it.email, it.password)
            }
    }

}
