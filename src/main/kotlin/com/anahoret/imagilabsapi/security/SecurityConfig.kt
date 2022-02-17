package com.anahoret.imagilabsapi.security

import com.anahoret.imagilabsapi.auth.web.JwtAuthorizationTokenFilter
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
@EnableConfigurationProperties(CorsSettings::class)
class SecurityConfig(
    private val unauthorizedHandler: AuthenticationEntryPoint,
    private val authorizationTokenFilter: JwtAuthorizationTokenFilter
) : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http
            .csrf().disable()
            .cors()
            .and()
            .exceptionHandling().authenticationEntryPoint(unauthorizedHandler)
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            // Index
            .antMatchers(HttpMethod.GET, "/").permitAll()

            // Auth
            .antMatchers(HttpMethod.POST, "/api/auth/teacher").permitAll()
            .antMatchers(HttpMethod.POST, "/api/auth/student").permitAll()
            .antMatchers(HttpMethod.POST, "/api/auth/logout").permitAll()

            // Swagger Documentation
            .antMatchers(HttpMethod.GET, "/swagger-ui.html").permitAll()
            .antMatchers(HttpMethod.GET, "/swagger-ui/**").permitAll()
            .antMatchers(HttpMethod.GET, "/v3/api-docs").permitAll()
            .antMatchers(HttpMethod.GET, "/v3/api-docs/swagger-config").permitAll()
            .anyRequest().authenticated()

        http.addFilterBefore(authorizationTokenFilter, UsernamePasswordAuthenticationFilter::class.java)
    }

}
