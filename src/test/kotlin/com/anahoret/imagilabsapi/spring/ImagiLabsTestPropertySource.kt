package com.anahoret.imagilabsapi.spring

import org.springframework.test.context.TestPropertySource

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@TestPropertySource(
    properties = [
        "jwt.secret = test_jwt_secret",
        "email.no-reply-address = noreply@example.com",
        "python-compiler.api-url = ",
        "python-compiler.api-token = "
    ]
)
annotation class ImagiLabsTestPropertySource
