package com.plcoding.ktspringboot.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(private val jwtService: JwtService): OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            if (jwtService.validateAccessToken(authHeader)) {
                val userId = jwtService.getUserIdFromToken(authHeader)
                // Added Authorities list to set authenticated = true
                val auth = UsernamePasswordAuthenticationToken(userId, null, emptyList())
                // Set the authentication to the SecurityContext to access it in other controllers
                SecurityContextHolder.getContext().authentication = auth
            } else {
                println(">>>>>Invalid JWT token")
                //return
                //throw ResponseStatusException(HttpStatusCode.valueOf(401),"User not logged in")
            }
        }
        filterChain.doFilter(request, response)
    }
}