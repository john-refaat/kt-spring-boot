package com.plcoding.ktspringboot.security

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.FilterChain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder

class JwtAuthFilterTest {

    private val jwtService = mockk<JwtService>()
    private val publicEndpoints = listOf("/public/**")
    private val authFilter = JwtAuthFilter(jwtService, publicEndpoints)

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `should skip authentication for public endpoints`() {
        val request = MockHttpServletRequest("GET", "/public/test")
        request.addHeader("Authorization", "Bearer test")
        val response = mockk<MockHttpServletResponse>()
        val chain = mockk<FilterChain>(relaxed = true)

        authFilter.doFilter(request, response, chain)

        assertNull(SecurityContextHolder.getContext().authentication)
        verify(exactly = 1) { chain.doFilter(request, response) }
        verify(exactly = 0) { jwtService.validateAccessToken(any()) }
        verify(exactly = 0) { jwtService.getUserIdFromToken(any()) }

    }

    @Test
    fun `should authenticate valid JWT`() {
        val request = MockHttpServletRequest("GET", "/secure/test")
        request.addHeader("Authorization", "Bearer valid-token")
        val response = mockk<MockHttpServletResponse>()
        val chain = mockk<FilterChain>(relaxed = true)

        every { jwtService.validateAccessToken(any()) } returns true
        every { jwtService.getUserIdFromToken(any()) } returns "test-user-id"

        authFilter.doFilter(request, response, chain)

        val auth = SecurityContextHolder.getContext().authentication
        assertNotNull(auth)
        assertTrue(auth.isAuthenticated)
        assertEquals("test-user-id", auth.name)
        verify(exactly = 1) { chain.doFilter(request, response) }
        verify(exactly = 1) { jwtService.validateAccessToken(any()) }
        verify(exactly = 1) { jwtService.getUserIdFromToken(any()) }

    }

    @Test
    fun `should not authenticate invalid JWT`() {
        val request = MockHttpServletRequest("GET", "/secure/test")
        request.addHeader("Authorization", "Bearer invalid-token")
        val response = mockk<MockHttpServletResponse>()
        val chain = mockk<FilterChain>(relaxed = true)
        every { jwtService.validateAccessToken(any()) } returns false
        authFilter.doFilter(request, response, chain)
        assertNull(SecurityContextHolder.getContext().authentication)
        verify(exactly = 1) { chain.doFilter(request, response) }
        verify(exactly = 1) { jwtService.validateAccessToken(any()) }
        verify(exactly = 0) { jwtService.getUserIdFromToken(any()) }
    }

}