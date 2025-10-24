package com.anahoret.imagilabsapi.security

import com.anahoret.imagilabsapi.auth.web.JwtAuthorizationTokenFilter
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@EnableConfigurationProperties(CorsSettings::class)
class SecurityConfig(
    private val unauthorizedHandler: AuthenticationEntryPoint,
    private val authorizationTokenFilter: JwtAuthorizationTokenFilter
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors {}
            .exceptionHandling { it.authenticationEntryPoint(unauthorizedHandler) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it
                    // Index
                    .requestMatchers(HttpMethod.GET, "/").permitAll()

                    // Auth
                    .requestMatchers(HttpMethod.POST, "/api/auth/teacher").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/auth/student").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/auth/logout").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/auth/teacher/forgot-password").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/auth/teacher/reset-password").permitAll()

                    // EdLink SSO
                    .requestMatchers(HttpMethod.POST, "/api/oauth/edlink/state").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/oauth/edlink/callback").permitAll()

                    // Sign up
                    .requestMatchers(HttpMethod.POST, "/api/sign-up/teacher").permitAll()

                    // Swagger Documentation
                    .requestMatchers(HttpMethod.GET, "/swagger-ui.html").permitAll()
                    .requestMatchers(HttpMethod.GET, "/swagger-ui/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/v3/api-docs").permitAll()
                    .requestMatchers(HttpMethod.GET, "/v3/api-docs/swagger-config").permitAll()
                    .anyRequest().authenticated()
            }

        http.addFilterBefore(authorizationTokenFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }

    @Bean
    fun corsConfigurer(corsSettings: CorsSettings): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/api/**")
                    .allowCredentials(true)
                    .allowedOrigins(*corsSettings.allowedOrigins)
                    .exposedHeaders("Content-Disposition")
                    .allowedHeaders("Content-Type", "Authorization")
                    .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            }
        }
    }

}
