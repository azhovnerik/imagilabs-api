package com.anahoret.imagilabsapi.common.storage

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.util.*

@EntityListeners(AuditingEntityListener::class)
@MappedSuperclass
abstract class BaseEntity(
    @Column(name = "created_at")
    @CreatedDate
    var createdAt: Long? = null,

    @Column(name = "last_modified_at")
    @LastModifiedDate
    var lastModifiedAt: Long? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID? = null
)
