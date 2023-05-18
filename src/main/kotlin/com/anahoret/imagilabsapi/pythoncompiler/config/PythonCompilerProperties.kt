package com.anahoret.imagilabsapi.pythoncompiler.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties("python-compiler")
class PythonCompilerProperties @ConstructorBinding constructor(
    val apiUrl: String,
    val apiToken: String
) {

    val apiTokenHeaderName: String = "apiKey"
}
