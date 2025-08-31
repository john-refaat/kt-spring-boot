package com.plcoding.ktspringboot.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class JwtService(
    @param:Value("\${jwt.secret}")
    private val jwtSecretEncoded: String //The Base64 encoded secret key
) {
    //The decoded secret key used to sign and verify the JWT (Used to generate The 3rd component of the Token)
    private val secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecretEncoded))
    val accessTokenValidityMs = 15L * 60L * 1000L   // 15 minutes
    val refreshTokenValidityMs = 1 * 60L * 60L * 1000L //1 hour

    private fun generateToken(userId: String, type: String, expiry: Long): String {
        val now = Date()
        val expiryDate = Date(now.time + expiry)
        return Jwts.builder()
            .subject(userId)
            .claim("type", type)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()

    }

    // Used to generate the access token
    // This token is used to validate the user's identity
    fun generateAccessToken(userId: String): String = generateToken(userId, "access", accessTokenValidityMs)

    // Used to generate the refresh token
    // This token is used to refresh the access token
    fun generateRefreshToken(userId: String): String = generateToken(userId, "refresh", refreshTokenValidityMs)

    fun validateAccessToken(token: String): Boolean {
        val claims = parseAllClaims(token) ?: return false
        val tokenType = claims["type"] as? String ?: return false
        return claims.subject != null && tokenType == "access"
                //&& claims.expiration.after(Date())
    }

    fun validateRefreshToken(token: String): Boolean {
        val claims = parseAllClaims(token) ?: return false
        val tokenType = claims["type"] as? String ?: return false
        return claims.subject != null && tokenType == "refresh"
    }

    fun getUserIdFromToken(token: String): String {
        return parseAllClaims(token)?.subject ?: throw IllegalArgumentException("Invalid token")
    }

    fun getExpirationDateFromToken(token: String): Date {
        return parseAllClaims(token)?.expiration ?: throw IllegalArgumentException("Invalid token")
    }

    // Authorization: Bearer <token>
    private fun getRawToken(token: String): String {
        return if (token.startsWith("Bearer "))
            token.removePrefix("Bearer ") else token
    }

    private fun parseAllClaims(token: String): Claims? {
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(getRawToken(token))
                .payload
        } catch (e: Exception) {
            null
        }
    }
}