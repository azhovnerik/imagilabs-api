package com.anahoret.imagilabsapi.teachers.export.clevertap.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate

@Configuration
@EnableConfigurationProperties(ClevertapProperties::class)
class ClevertapConfig {

    @Bean
    fun clevertapRestTemplate(properties: ClevertapProperties): RestTemplate {
        return RestTemplateBuilder()
            .rootUri(properties.apiUrl)
            .defaultHeader(properties.apiProjectHeaderName, properties.apiProjectId)
            .defaultHeader(properties.apiPasscodeHeaderName, properties.apiPasscode)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
    }
}
