package com.anahoret.imagilabsapi.security

import org.springframework.stereotype.Component
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Component
class CorsConfigurer(private val corsSettings: CorsSettings) : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowCredentials(true)
            .allowedOrigins(*corsSettings.allowedOrigins)
            .allowedHeaders("Content-Type", "Authorization", "Content-Disposition")
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
    }

}
