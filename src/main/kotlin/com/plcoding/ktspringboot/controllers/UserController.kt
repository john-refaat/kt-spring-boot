package com.plcoding.ktspringboot.controllers

import com.plcoding.ktspringboot.repository.UserRepository
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
    private val userRepository: UserRepository
) {

    private val logger = KotlinLogging.logger {  }

    
    @GetMapping("/me")
    fun getAuthenticatedUser(): Map<String, String> {
        val authentication = SecurityContextHolder.getContext().authentication
        val user = userRepository.findById(ObjectId(authentication.name))
            .orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "User Not Found")
            }
        logger.info("Logged-in user: $user")
        return mapOf("message" to "Hello ${user.email}")
    }

}