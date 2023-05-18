package com.anahoret.imagilabsapi.email

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties("email")
class EmailProperties @ConstructorBinding constructor(
    val noReplyAddress: String
)
