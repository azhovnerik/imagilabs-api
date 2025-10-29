package com.anahoret.imagilabsapi.lovable.storage

import com.anahoret.imagilabsapi.common.storage.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "lovable_accounts")
class LovableAccountEntity(
    @Column(name = "email")
    var email: String,

    @Column(name = "password")
    var password: String,

    @Column(name = "username")
    var username: String,

    @Column(name = "connected_user")
    var connectedUser: UUID? = null,

    @Column(name = "connected_at")
    var connectedAt: Long? = null,

    @Column(name = "active")
    var active: Boolean = false,
) : BaseEntity()
