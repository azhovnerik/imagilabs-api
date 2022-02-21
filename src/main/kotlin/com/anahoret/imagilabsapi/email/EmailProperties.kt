package com.anahoret.imagilabsapi.email

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("email")
class EmailProperties(
    val noReplyAddress: String
)
