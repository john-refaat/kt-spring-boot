package com.plcoding.ktspringboot.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(private val jwtService: JwtService,
                    @field:Value("\${security.public-endpoints}")
                    private val publicEndpoints: List<String>): OncePerRequestFilter() {

    companion object {
        private const val BEARER_PREFIX = "Bearer "
        private const val AUTHORIZATION_HEADER = "Authorization"
    }

    private val pathMatcher = AntPathMatcher()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        logger.info { "Executing doFilterInternal..." }
        if (isPublicEndpoint(request.requestURI)) {
            filterChain.doFilter(request, response)
            return
        }
        val authHeader = request.getHeader(AUTHORIZATION_HEADER)
        if (authHeader != null && authHeader.startsWith( BEARER_PREFIX)) {
            processJwtAuthentication(authHeader)
        }
        filterChain.doFilter(request, response)
    }

    private fun processJwtAuthentication(authHeader: String) {
        logger.info { "Processing JWT Authentication..." }
        if (jwtService.validateAccessToken(authHeader)) {
            val userId = jwtService.getUserIdFromToken(authHeader)
            // Added Authorities list to set authenticated = true
            val auth = UsernamePasswordAuthenticationToken(userId, null, emptyList())
            // Set the authentication to the SecurityContext to access it in other controllers
            SecurityContextHolder.getContext().authentication = auth
        }
    }

    private fun isPublicEndpoint(requestURI: String): Boolean {
        return publicEndpoints.any { pathMatcher.match(it, requestURI) }
    }
}