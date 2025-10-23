package com.anahoret.imagilabsapi.edlink.api

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties("ed-link")
class EdLinkProperties @ConstructorBinding constructor(
    val apiUrl: String,
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String
)
