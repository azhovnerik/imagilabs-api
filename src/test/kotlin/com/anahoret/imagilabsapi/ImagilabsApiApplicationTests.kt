package com.anahoret.imagilabsapi

import com.anahoret.imagilabsapi.email.EmailService
import com.anahoret.imagilabsapi.spring.ImagiLabsTestPropertySource
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

@SpringBootTest
@ImagiLabsTestPropertySource
class ImagilabsApiApplicationTests {

    @MockBean
    lateinit var emailService: EmailService

    @Test
    fun contextLoads() {
    }

}
