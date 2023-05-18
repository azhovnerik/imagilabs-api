package com.anahoret.imagilabsapi.auth.web.jwt

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import java.time.Duration

@ConfigurationProperties(prefix = "jwt")
class JwtProperties @ConstructorBinding constructor(
    // Need to get JWT_SECRET from here instead of application.yml because of this issue
    // https://github.com/spring-projects/spring-boot/issues/8693
    @Value("\${JWT_SECRET}") val secret: String,
    val ttlWeb: Duration,
    val ttlMobile: Duration
) {

    companion object {

        const val JWT_REQUEST_ATTRIBUTE = "JWT_REQUEST_ATTRIBUTE"
    }
}
