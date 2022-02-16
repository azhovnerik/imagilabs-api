package com.anahoret.imagilabsapi.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("cors")
class CorsSettings(
    val allowedOrigins: Array<String>
)
