package com.anahoret.imagilabsapi.admins.domain

import com.anahoret.imagilabsapi.admins.storage.AdminProfileEntityRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

interface AdminProfileService {

    fun getAdminById(adminId: UUID): AdminProfile?
    fun getAdminCredentialsByEmail(email: String): AdminCredentials?
}

@Service
class AdminProfileServiceImpl(
    private val adminProfileEntityRepository: AdminProfileEntityRepository
) : AdminProfileService {

    override fun getAdminById(adminId: UUID): AdminProfile? {
        return adminProfileEntityRepository.findByIdOrNull(adminId)
            ?.let(AdminProfile.Companion::fromEntity)
    }

    override fun getAdminCredentialsByEmail(email: String): AdminCredentials? {
        return adminProfileEntityRepository.findByEmail(email)
            ?.let(AdminCredentials.Companion::fromEntity)
    }

}
