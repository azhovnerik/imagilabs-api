package com.anahoret.imagilabsapi.documentation

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springdoc.core.PropertyResolverUtils
import org.springdoc.core.SecurityService
import org.springdoc.core.customizers.OperationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.security.access.annotation.Secured
import java.lang.reflect.Method

@Configuration
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
class OpenApi30Config {

    @Bean
    fun securityParser(propertyResolverUtils: PropertyResolverUtils): SecurityService {
        return object : SecurityService(propertyResolverUtils) {
            override fun getSecurityRequirementsForMethod(
                method: Method,
                allSecurityTags: MutableSet<SecurityRequirement>?
            ): MutableSet<SecurityRequirement> {
                return (allSecurityTags ?: mutableSetOf())
                    .also { securityRequirements ->
                        getSecuredAnnotation(method)
                            ?.let { SecurityRequirement(name = "bearerAuth", scopes = emptyArray()) }
                            ?.let(securityRequirements::add)
                    }
                    .let { super.getSecurityRequirementsForMethod(method, it) }
            }

        }
    }

    @Bean
    fun roleOperationCustomizer(): OperationCustomizer {
        return OperationCustomizer { operation, handlerMethod ->
            val secured = getSecuredAnnotation(handlerMethod.method)
            val roles = secured?.value?.toList()?.toString() ?: "[ALL]"
            val description = operation.description
            operation.description = "Roles: $roles${description?.let { "<br/><br/>$it" }.orEmpty()}"
            operation
        }
    }

    private fun getSecuredAnnotation(method: Method): Secured? {
        return method.let {
            AnnotationUtils.findAnnotation(it, Secured::class.java)
                ?: AnnotationUtils.findAnnotation(it.declaringClass, Secured::class.java)
        }
    }

}
