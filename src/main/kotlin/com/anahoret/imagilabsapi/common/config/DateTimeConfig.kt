package com.anahoret.imagilabsapi.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
class DateTimeConfig {

    @Bean
    fun clock(): Clock {
        return Clock.systemUTC()
    }

}
