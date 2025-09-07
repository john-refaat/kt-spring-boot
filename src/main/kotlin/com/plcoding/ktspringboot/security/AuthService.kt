package com.plcoding.ktspringboot.security

import com.plcoding.ktspringboot.model.RefreshToken
import com.plcoding.ktspringboot.model.User
import com.plcoding.ktspringboot.repository.RefreshTokenRepository
import com.plcoding.ktspringboot.repository.UserRepository
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.security.MessageDigest
import java.time.Instant
import java.util.*

import mu.KotlinLogging

@Service
class AuthService(
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
    private val hashEncoder: HashEncoder,
    private val refreshTokenRepository: RefreshTokenRepository
) {
    private val logger = KotlinLogging.logger {}

    data class TokenPair(
        val accessToken: String,
        val refreshToken: String
    )

    fun register(email: String, password: String) {
        logger.info { "Attempting to register user with email: $email" }
        val user = userRepository.findByEmail(email)
        if (user != null) {
            logger.warn { "Registration failed - email already exists: $email" }
            throw ResponseStatusException(HttpStatus.CONFLICT, "This email is already associated with an account")
        }

        userRepository.save(
            User(
                email,
                hashEncoder.encode(password)
            )
        )
        logger.info { "Successfully registered new user with email: $email" }
    }

    fun login(email: String, password: String): TokenPair {
        logger.info { "Attempting login for user: $email" }
        val user = userRepository.findByEmail(email) ?: run {
            logger.warn { "Login failed - user not found: $email" }
            throw ResponseStatusException(HttpStatusCode.valueOf(401), "Invalid credentials")
        }

        if (!hashEncoder.matches(password, user.hashedPassword)) {
            logger.warn { "Login failed - invalid password for user: $email" }
            throw ResponseStatusException(HttpStatusCode.valueOf(401), "Invalid credentials")
        }

        val accessToken = jwtService.generateAccessToken(user.id.toHexString())
        val refreshToken = jwtService.generateRefreshToken(user.id.toHexString())
        refreshTokenRepository.deleteByUserRef(user.id)
        storeRefreshToken(user, refreshToken)
        logger.info { "Successfully logged in user: $email" }
        return TokenPair(accessToken, refreshToken)
    }

    @Transactional
    fun refresh(refreshToken: String): TokenPair {
        logger.info { "Attempting to refresh token" }
        if (!jwtService.validateRefreshToken(refreshToken)) {
            logger.warn { "Token refresh failed - invalid refresh token" }
            throw ResponseStatusException(HttpStatusCode.valueOf(401), "Invalid refresh token")
        }

        val userId = jwtService.getUserIdFromToken(refreshToken)
        val user = userRepository.findById(ObjectId(userId))
            .orElseThrow {
                logger.warn { "Token refresh failed - user not found for ID: $userId" }
                ResponseStatusException(HttpStatusCode.valueOf(401), "Invalid refresh token")
            }

        val newAccessToken = jwtService.generateAccessToken(userId)
        val newRefreshToken = jwtService.generateRefreshToken(userId)

        refreshTokenRepository.findByUserRefAndHashedToken(
            ObjectId(userId),
            hashToken(refreshToken)
        )
            ?.apply {
                refreshTokenRepository.deleteById(this.id)
            }
            ?: run {
                logger.warn { "Token refresh failed - token not found in repository for user ID: $userId" }
                throw ResponseStatusException(
                    HttpStatusCode.valueOf(401),
                    "Refresh Token not recognized. Could be expired or used"
                )
            }

        storeRefreshToken(user, newRefreshToken)
        logger.info { "Successfully refreshed token for user ID: $userId" }
        return TokenPair(newAccessToken, newRefreshToken)
    }

    private fun storeRefreshToken(user: User, refreshToken: String) {
        refreshTokenRepository.save(
            RefreshToken(
                user.id,
                hashToken(refreshToken),
                Instant.now().plusMillis(jwtService.refreshTokenValidityMs)
            )
        )
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(token.encodeToByteArray())
        return Base64.getEncoder().encodeToString(bytes)
    }
}