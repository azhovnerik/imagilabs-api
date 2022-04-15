package com.anahoret.imagilabsapi.applicationproperties.storage

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "application_properties")
class ApplicationPropertyEntity(

    @Id
    @Column(name = "key")
    var key: String,

    @Column(name = "value")
    var value: String,
)
