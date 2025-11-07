package com.anahoret.imagilabsapi.lovable.domain

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.lovable.storage.LovableAccountRepository
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.util.*

interface LovableAccountService {
    fun getActive(user: UserProfile): LovableAccount?
    fun connectToUser(user: UserProfile): LovableAccount?
    fun connectedCount(userId: UUID): Long
    fun getByConnectedUsers(userIds: List<UUID>): List<LovableAccount>
    fun deleteByIds(userIds: List<UUID>)
}

@Service
class LovableAccountServiceImpl(
    private val lovableAccountRepository: LovableAccountRepository,
    private val studentProfileService: StudentProfileService,
    private val clock: Clock
) : LovableAccountService {

    @Transactional
    override fun connectToUser(user: UserProfile): LovableAccount? {
        return when (user.userType) {
            UserType.TEACHER, UserType.STUDENT -> doConnect(user)
            UserType.ADMIN -> throw IllegalArgumentException("Cannot connect admin")
        }
    }

    override fun connectedCount(userId: UUID): Long {
        return lovableAccountRepository.countByConnectedUser(userId)
    }

    override fun getByConnectedUsers(userIds: List<UUID>): List<LovableAccount> {
        if (userIds.isEmpty()) return emptyList()
        val students = studentProfileService.listByIds(userIds).associateBy(StudentProfile::id)
        return lovableAccountRepository.findByConnectedUserInAndActiveTrue(userIds).mapNotNull {
            it.connectedUser?.let { id ->
                LovableAccount(it.connectedUser, students.getValue(id).name, it.username, it.email, it.password)
            }
        }
    }

    override fun deleteByIds(userIds: List<UUID>) {
        if (userIds.isEmpty()) return
        lovableAccountRepository.deleteByConnectedUserIn(userIds)
    }

    override fun getActive(user: UserProfile): LovableAccount? {
        return lovableAccountRepository.findOneByConnectedUserAndActiveTrue(user.id)
            ?.let { LovableAccount(it.connectedUser, user.fullName, it.username, it.email, it.password) }
    }

    private fun doConnect(user: UserProfile): LovableAccount? {
        return lovableAccountRepository.findFirstByConnectedUserIsNull()
            ?.let { lovableAccountEntity ->
                lovableAccountRepository.findByConnectedUser(user.id)
                    .onEach { it.active = false }
                    .let(lovableAccountRepository::saveAll)

                lovableAccountEntity.connectedUser = user.id
                lovableAccountEntity.connectedAt = clock.millis()
                lovableAccountEntity.active = true
                lovableAccountRepository.save(lovableAccountEntity)

                lovableAccountRepository.findOneByConnectedUserAndActiveTrue(user.id)
                    ?.let { LovableAccount(it.connectedUser, user.fullName, it.username, it.email, it.password) }
            }
    }

}
