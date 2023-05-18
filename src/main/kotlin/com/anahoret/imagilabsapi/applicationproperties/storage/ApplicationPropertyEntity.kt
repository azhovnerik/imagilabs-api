package com.anahoret.imagilabsapi.applicationproperties.storage

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "application_properties")
class ApplicationPropertyEntity(

    @Id
    @Column(name = "key")
    var key: String,

    @Column(name = "value")
    var value: String,
)
