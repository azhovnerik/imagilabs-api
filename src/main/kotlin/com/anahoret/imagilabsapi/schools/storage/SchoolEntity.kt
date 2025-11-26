package com.anahoret.imagilabsapi.schools.storage

import com.anahoret.imagilabsapi.common.storage.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "schools")
class SchoolEntity(
    @Column(name = "name", nullable = false)
    var name: String
) : BaseEntity()