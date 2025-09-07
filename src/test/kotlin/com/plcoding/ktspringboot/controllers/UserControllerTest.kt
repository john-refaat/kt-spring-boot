package com.plcoding.ktspringboot.controllers

import com.plcoding.ktspringboot.model.User
import com.plcoding.ktspringboot.service.UserService
import io.mockk.every
import io.mockk.mockk
import org.bson.types.ObjectId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.server.ResponseStatusException

class UserControllerTest {

    private val userService = mockk<UserService>()
    private val controller = UserController(userService)

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `getAuthenticatedUser returns UserResponse when user exists`() {
        // Given
        val userId = ObjectId.get()
        val email = "john@example.com"
        val password = "hashedPassword"
        val auth = UsernamePasswordAuthenticationToken(userId.toHexString(), null, listOf())

        val userResponse = UserController.UserResponse(email)
        SecurityContextHolder.getContext().authentication = auth

        val user = User(id = userId, email = email, hashedPassword = password)
        every { userService.getUserById(userId.toHexString()) } returns user

        // When
        val result = controller.getAuthenticatedUser()

        // Then
        assertNotNull(result)
        assert(
            result == userResponse
        )
        assert(
            result.email == user.email
        )
    }

    @Test
    fun `getAuthenticatedUser throws NOT_FOUND when user is missing`() {
        // given authentication with id "456"
        val auth = UsernamePasswordAuthenticationToken("456", null)
        SecurityContextHolder.getContext().authentication = auth

        // and user service returns null
        every { userService.getUserById("456") } returns null

        // when + then
        val ex = assertThrows(ResponseStatusException::class.java) {
            controller.getAuthenticatedUser()
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
        assertEquals("User not found", ex.reason)
    }
    }