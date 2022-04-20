package com.anahoret.imagilabsapi.pythoncompiler.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate

@Configuration
@EnableConfigurationProperties(PythonCompilerProperties::class)
class PythonCompilerConfig {

    @Bean
    fun pythonCompilerRestTemplate(pythonCompilerProperties: PythonCompilerProperties): RestTemplate {
        return RestTemplateBuilder()
            .rootUri(pythonCompilerProperties.apiUrl)
            .defaultHeader(pythonCompilerProperties.apiTokenHeaderName, pythonCompilerProperties.apiToken)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
    }

}
