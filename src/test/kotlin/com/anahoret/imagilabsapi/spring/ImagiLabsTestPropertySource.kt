package com.anahoret.imagilabsapi.spring

import org.springframework.test.context.TestPropertySource

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@TestPropertySource(
    properties = [
        "jwt.secret = test_jwt_secret",
    ]
)
annotation class ImagiLabsTestPropertySource()
