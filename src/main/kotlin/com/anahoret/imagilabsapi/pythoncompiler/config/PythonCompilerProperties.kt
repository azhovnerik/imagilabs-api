package com.anahoret.imagilabsapi.pythoncompiler.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("python-compiler")
class PythonCompilerProperties(
    val apiUrl: String,
    val apiToken: String
) {

    val apiTokenHeaderName: String = "apiKey"
}
