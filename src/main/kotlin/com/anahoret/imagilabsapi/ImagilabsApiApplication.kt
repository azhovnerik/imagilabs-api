package com.anahoret.imagilabsapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.function.context.config.ContextFunctionCatalogAutoConfiguration

@SpringBootApplication(
    exclude = [ContextFunctionCatalogAutoConfiguration::class]
)
class ImagilabsApiApplication

fun main(args: Array<String>) {
    runApplication<ImagilabsApiApplication>(*args)
}
