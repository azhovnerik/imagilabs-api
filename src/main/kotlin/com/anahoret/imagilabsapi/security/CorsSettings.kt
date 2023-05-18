package com.anahoret.imagilabsapi.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties("cors")
class CorsSettings @ConstructorBinding constructor(
    val allowedOrigins: Array<String>
)
