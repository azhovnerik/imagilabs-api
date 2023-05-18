package com.anahoret.imagilabsapi.projects.storage

import com.anahoret.imagilabsapi.common.storage.BaseEntity
import com.anahoret.imagilabsapi.users.UserType
import java.util.*
import jakarta.persistence.*

@Entity
@Table(name = "projects")
class ProjectEntity(
    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "owner_id", nullable = false)
    var ownerId: UUID,

    @Column(name = "owner_user_type", nullable = false)
    @Enumerated(EnumType.STRING)
    var ownerUserType: UserType,

    @Column(name = "source_code", nullable = false)
    var sourceCode: String,

    @Column(name = "run_result")
    var runResult: String? = null
) : BaseEntity()
