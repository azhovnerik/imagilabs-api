package com.anahoret.imagilabsapi.acceptance.features

import com.anahoret.imagilabsapi.email.EmailService
import com.anahoret.imagilabsapi.spring.ImagiLabsTestPropertySource
import io.cucumber.spring.CucumberContextConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean

@CucumberContextConfiguration
@SpringBootTest
@ImagiLabsTestPropertySource
@AutoConfigureMockMvc
class CucumberSpringTest {

    @MockitoBean
    lateinit var emailService: EmailService
}
