package com.plcoding.ktspringboot.controllers

import com.plcoding.ktspringboot.model.User
import com.plcoding.ktspringboot.service.UserService
import mu.KotlinLogging
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/user")
class UserController(
    private val userService: UserService
) {

    private val logger = KotlinLogging.logger { }

    data class UserResponse(val email: String)

    @GetMapping("/me")
    fun getAuthenticatedUser(): UserResponse {
        val authentication = SecurityContextHolder.getContext().authentication
        val user = userService.getUserById(authentication.name) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "User not found"
        )
        logger.info("Logged-in user: $user")
        return UserResponse(user.email)
    }

}