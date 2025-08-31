package com.plcoding.ktspringboot.controllers

import com.plcoding.ktspringboot.repository.UserRepository
import com.plcoding.ktspringboot.security.AuthService
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService,
    private val userRepository: UserRepository) {

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

    @RequestMapping("/register")
    fun register(@Valid @RequestBody registerRequest: RegisterRequest) {
        authService.register(registerRequest.email, registerRequest.password)
    }

    @RequestMapping("/login")
    fun login(@Valid @RequestBody authRequest: AuthRequest): AuthService.TokenPair {
        return authService.login(authRequest.email, authRequest.password)
    }

    @RequestMapping("/refresh")
    fun refresh(@RequestBody refreshRequest: RefreshRequest): AuthService.TokenPair {
        return authService.refresh(refreshRequest.refreshToken)
    }


}