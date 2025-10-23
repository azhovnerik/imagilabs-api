package com.anahoret.imagilabsapi.edlink.api

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.web.client.RestTemplate

@Configuration
@EnableConfigurationProperties(EdLinkProperties::class)
class EdLinkConfig {

    @Bean
    fun edLinkRestTemplate(edLinkProperties: EdLinkProperties): RestTemplate {
        return RestTemplateBuilder()
            .rootUri(edLinkProperties.apiUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer ${edLinkProperties.clientSecret}")
            .build()
    }

}
