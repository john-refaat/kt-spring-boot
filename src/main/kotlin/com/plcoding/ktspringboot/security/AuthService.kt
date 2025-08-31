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

@Service
class AuthService(

    private val jwtService: JwtService,
    private val userRepository: UserRepository,
    private val hashEncoder: HashEncoder,
    private val refreshTokenRepository: RefreshTokenRepository
) {

    data class TokenPair(
        val accessToken: String,
        val refreshToken: String
    )

    fun register(email: String, password: String) {
        val user = userRepository.findByEmail(email)
        if (user != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT,"This email is already associated with an account")        }

        userRepository.save(User(email,
            hashEncoder.encode(password)))
    }

    fun login(email: String, password: String): TokenPair {
        val user = userRepository.findByEmail(email)?: throw ResponseStatusException(HttpStatusCode.valueOf(401),"Invalid credentials")

        if (!hashEncoder.matches(password, user.hashedPassword))
            throw ResponseStatusException(HttpStatusCode.valueOf(401),"Invalid credentials")

        val accessToken = jwtService.generateAccessToken(user.id.toHexString())
        val refreshToken = jwtService.generateRefreshToken(user.id.toHexString())

        storeRefreshToken(user, refreshToken)
        return TokenPair(accessToken, refreshToken)
    }

    @Transactional
    fun refresh(refreshToken: String): TokenPair {
        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw ResponseStatusException(HttpStatusCode.valueOf(401),"Invalid refresh token")
        }

        val userId = jwtService.getUserIdFromToken(refreshToken)
        val user = userRepository.findById(ObjectId(userId))
            .orElseThrow { ResponseStatusException(HttpStatusCode.valueOf(401),"Invalid Refresh Token") }

        val newAccessToken = jwtService.generateAccessToken(userId)
        val newRefreshToken = jwtService.generateRefreshToken(userId)

        refreshTokenRepository.findByUserRefAndHashedToken(ObjectId(userId),
            hashToken(refreshToken))
            ?.apply {
                refreshTokenRepository.deleteById(this.id)
            }
            ?: throw ResponseStatusException(HttpStatusCode.valueOf(401),"Refresh Token not recognized. Could be expired or used")

        storeRefreshToken(user, newRefreshToken)
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