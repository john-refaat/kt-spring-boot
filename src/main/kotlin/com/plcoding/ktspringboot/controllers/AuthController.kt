package com.plcoding.ktspringboot.controllers

import com.plcoding.ktspringboot.repository.UserRepository
import com.plcoding.ktspringboot.security.AuthService
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService,
    private val userRepository: UserRepository) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    data class RegisterRequest(
        @field:Email(message = "Not a valid email")
        val email: String,
        @field:NotBlank(message = "Password must not be blank")
        @field:Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}$",
            message = "Password must be at least 8 characters long and contain at least one lowercase letter, one uppercase letter, one digit, and one special character")
        val password: String
    )

    data class AuthRequest(
        @field:Email(message = "Not a valid email")
        val email: String,
        @field:NotBlank(message = "Password must not be blank")
        val password: String
    )

    data class RefreshRequest(
        val refreshToken: String
    )

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping("/register")
    fun register(@Valid @RequestBody registerRequest: RegisterRequest) {
        logger.info { "Attempting to register user with email: ${registerRequest.email}" }
        authService.register(registerRequest.email, registerRequest.password)
        logger.info { "Successfully registered user with email: ${registerRequest.email}" }
    }

    @RequestMapping("/login")
    fun login(@Valid @RequestBody authRequest: AuthRequest): AuthService.TokenPair {
        logger.info { "Login attempt for user: ${authRequest.email}" }
        val tokenPair = authService.login(authRequest.email, authRequest.password)
        logger.info { "Successfully logged in user: ${authRequest.email}" }
        return tokenPair
    }

    @RequestMapping("/refresh")
    fun refresh(@RequestBody refreshRequest: RefreshRequest): AuthService.TokenPair {
        logger.info { "Token refresh requested" }
        val tokenPair = authService.refresh(refreshRequest.refreshToken)
        logger.info { "Token refresh successful" }
        return tokenPair
    }


}