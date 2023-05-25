package com.anahoret.imagilabsapi.common.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(DomainProperties::class)
class DomainConfig
