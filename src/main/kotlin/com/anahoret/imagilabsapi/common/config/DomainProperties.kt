package com.anahoret.imagilabsapi.common.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties("domains")
class DomainProperties @ConstructorBinding constructor(
    val web: String?
)
