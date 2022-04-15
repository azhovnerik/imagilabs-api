package com.anahoret.imagilabsapi.applicationproperties.storage

import org.springframework.data.repository.CrudRepository

interface ApplicationPropertiesRepository : CrudRepository<ApplicationPropertyEntity, String>
