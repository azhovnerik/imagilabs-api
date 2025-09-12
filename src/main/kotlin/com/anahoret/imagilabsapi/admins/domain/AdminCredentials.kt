package com.anahoret.imagilabsapi.admins.domain

import com.anahoret.imagilabsapi.admins.storage.AdminProfileEntity
import java.util.*

class AdminCredentials(
    val id: UUID,
    val email: String,
    val passwordHash: String
) {

    companion object {

        fun fromEntity(entity: AdminProfileEntity): AdminCredentials {
            return with(entity) {
                AdminCredentials(id!!, email, passwordHash)
            }
        }
    }
}
