package com.anahoret.imagilabsapi

import com.anahoret.imagilabsapi.spring.ImagiLabsTestPropertySource
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@ImagiLabsTestPropertySource
class ImagilabsApiApplicationTests {

    @Test
    fun contextLoads() {
    }

}
