package com.plcoding.ktspringboot.controllers

import com.plcoding.ktspringboot.model.User
import com.plcoding.ktspringboot.security.JwtAuthFilter
import com.plcoding.ktspringboot.security.JwtService
import com.plcoding.ktspringboot.service.UserService
import io.mockk.every
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

private const val TEST_USER_ID = "507f1f77bcf86cd799439011"
private const val TEST_USER_ID_NOT_FOUND = "507f1f77bcf86cd799439012"
private const val TEST_EMAIL = "test@example.com"
private const val TEST_HASHED_PASSWORD = "hashedPassword"


@WebMvcTest(UserController::class)
@AutoConfigureMockMvc(addFilters = false) // ✅ disables security filters
class UserControllerMockMVCTest(@Autowired val mockMvc: MockMvc) {

    @MockitoBean
    private lateinit var userService: UserService

    @MockitoBean
    private lateinit var jwtAuthFilter: JwtAuthFilter

    @MockitoBean
    private lateinit var jwtService: JwtService

    @Test
    @WithMockUser(username = TEST_USER_ID, roles = [])
    fun `getAuthenticatedUser returns UserResponse when user exists`() {
        val user = User(id = ObjectId(TEST_USER_ID), email = TEST_EMAIL, hashedPassword = TEST_HASHED_PASSWORD)

        `when`(userService.getUserById(TEST_USER_ID)).thenReturn(user)

        mockMvc.get("/user/me")
            .andExpect {
                status { isOk() }
                jsonPath("$.email") { value(TEST_EMAIL) }
            }
    }

    @Test
    @WithMockUser(username = TEST_USER_ID_NOT_FOUND, roles = [])
    fun `getAuthenticatedUser returns 404 Not Found when user does not exist`() {
        `when`(userService.getUserById(TEST_USER_ID_NOT_FOUND)).thenReturn(null)
        mockMvc.get("/user/me")
            .andExpect {
                status { isNotFound() }
            }
    }
}