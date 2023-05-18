package com.anahoret.imagilabsapi.admins.storage

import com.anahoret.imagilabsapi.common.storage.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "admin_profiles")
class AdminProfileEntity(
    @Column(name = "email", nullable = false, unique = true)
    var email: String,

    @Column(name = "password_hash", nullable = false)
    var passwordHash: String,

    @Column(name = "name", nullable = false)
    var name: String,
) : BaseEntity()
