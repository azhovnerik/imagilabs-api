package com.anahoret.imagilabsapi.auth.web.jwt

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class JwtConfig
