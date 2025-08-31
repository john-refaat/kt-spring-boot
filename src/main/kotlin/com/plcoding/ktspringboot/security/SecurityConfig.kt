package com.plcoding.ktspringboot.security

import jakarta.servlet.DispatcherType
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
class SecurityConfig(
    val jwtAuthFilter: JwtAuthFilter,
    @field:Value("\${security.public-endpoints}")
    private val publicEndpoints: List<String>
) {

    @Bean
    fun filterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        return httpSecurity.csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                var registry = it
                for (endpoint in publicEndpoints)
                    registry = registry.requestMatchers(endpoint).permitAll()
                registry.dispatcherTypeMatchers(
                    DispatcherType.ERROR,
                    DispatcherType.FORWARD,
                ).permitAll()
                .anyRequest().authenticated()
            }
            .exceptionHandling { configurer ->
                configurer.authenticationEntryPoint(
                    HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }

}