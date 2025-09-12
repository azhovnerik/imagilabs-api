package com.anahoret.imagilabsapi

import com.anahoret.imagilabsapi.email.EmailService
import com.anahoret.imagilabsapi.spring.ImagiLabsTestPropertySource
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest
@ImagiLabsTestPropertySource
class ImagilabsApiApplicationTests {

    @MockitoBean
    lateinit var emailService: EmailService

    @Suppress("EmptyMethod")
    @Test
    fun contextLoads() {
    }

}
