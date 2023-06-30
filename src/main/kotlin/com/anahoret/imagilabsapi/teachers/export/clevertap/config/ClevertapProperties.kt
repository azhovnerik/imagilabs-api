package com.anahoret.imagilabsapi.teachers.export.clevertap.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties("clevertap")
class ClevertapProperties @ConstructorBinding constructor(
    val apiUrl: String,
    val apiProjectHeaderName: String,
    val apiProjectId: String,
    val apiPasscodeHeaderName: String,
    val apiPasscode: String
)
