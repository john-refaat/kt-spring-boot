package com.plcoding.ktspringboot.security

import com.plcoding.ktspringboot.model.RefreshToken
import com.plcoding.ktspringboot.model.User
import com.plcoding.ktspringboot.repository.RefreshTokenRepository
import com.plcoding.ktspringboot.repository.UserRepository
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.util.StringUtils
import org.springframework.web.server.ResponseStatusException
import java.util.Optional

private const val user_exists_msg = "This email is already associated with an account"

private const val email = "test@example.com"
private const val password = "password"
private const val hashedPassword = "hashedPassword"

class AuthServiceTest {

    val jwtService = mockk<JwtService>()
    val userRepository = mockk<UserRepository>()
    val hashEncoder = mockk<HashEncoder>()
    val refreshTokenRepository = mockk<RefreshTokenRepository>()

    lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        authService = AuthService(jwtService, userRepository, hashEncoder, refreshTokenRepository)
    }

    @Test
    fun `register should save new user when email does not exist`() {
        // given
        every { userRepository.findByEmail(email) } returns null
        every { userRepository.save(any()) } returns mockk()
        every { hashEncoder.encode(password) } returns hashedPassword

        // when
        authService.register(email, password)

        // then
        verify(exactly = 1) { userRepository.save(match { it.email == email && it.hashedPassword == hashedPassword }) }
        verify(exactly = 1) { hashEncoder.encode(match { it == password }) }
    }

    @Test
    fun `register should throw conflict when email already exists`() {
        // given
        every { userRepository.findByEmail(any()) } returns mockk<User>()

        // when + then
        val exception = assertThrows<ResponseStatusException> {
            authService.register(email, password)
        }
        assertEquals(HttpStatus.CONFLICT, exception.statusCode)
        assertEquals(user_exists_msg, exception.reason)
    }

    @Test
    fun `login should return token pair on success`() {
        // given

        val accessToken = "access-token"
        val refreshToken = "refresh-token"
        val userId = ObjectId.get()
        every { userRepository.findByEmail(email) } returns User(email, "hashedPassword", id = userId)
        every { hashEncoder.matches(any(), any()) } returns true
        every { jwtService.generateAccessToken(any()) } returns accessToken
        every { jwtService.generateRefreshToken(any()) } returns refreshToken
        every { refreshTokenRepository.deleteByUserRef(any()) } just Runs
        every { jwtService.refreshTokenValidityMs } returns 1000000L
        every { refreshTokenRepository.save(any()) } returns mockk()


        // when
        val tokenPair = authService.login(email, password)

        // then
        assertEquals(accessToken, tokenPair.accessToken)
        assertEquals(refreshToken, tokenPair.refreshToken)
        verify(exactly = 1) { hashEncoder.matches(password, match { it == "hashedPassword" }) }
        verify(exactly = 1) { jwtService.generateAccessToken(any()) }
        verify(exactly = 1) { jwtService.generateRefreshToken(any()) }
        verify(exactly = 1) { userRepository.findByEmail(email) }
        verify(exactly = 1) { refreshTokenRepository.deleteByUserRef(match { it == userId }) }
        verify(exactly = 1) { refreshTokenRepository.save(match { it.userRef == userId && StringUtils.hasText(it.hashedToken) }) }
    }

    @Test
    fun `login should throw when user not found`() {
        // given
        every { userRepository.findByEmail(email) } returns null

        // when + then
        val exception = assertThrows<ResponseStatusException> {
            authService.login(email, "password")
        }
        assertEquals(HttpStatus.UNAUTHORIZED, exception.statusCode)
        assertEquals("Invalid credentials", exception.reason)
        verify(exactly = 1) { userRepository.findByEmail(email) }
        verify(exactly = 0) { hashEncoder.matches(any(), any()) }
        verify(exactly = 0) { jwtService.generateAccessToken(any()) }
        verify(exactly = 0) { jwtService.generateRefreshToken(any()) }
        verify(exactly = 0) { refreshTokenRepository.deleteByUserRef(any()) }
        verify(exactly = 0) { refreshTokenRepository.save(any()) }
    }

    @Test
    fun `login should throw when password does not match`() {
        // given
        val user = User(email, hashedPassword)
        val email = user.email
        every { userRepository.findByEmail(email) } returns user
        every { hashEncoder.matches(any(), any()) } returns false

        // when + then
        val exception = assertThrows<ResponseStatusException> {
            authService.login(email, password)
        }
        assertEquals(HttpStatus.UNAUTHORIZED, exception.statusCode)
        assertEquals("Invalid credentials", exception.reason)
        verify(exactly = 1) { userRepository.findByEmail(email) }
        verify(exactly = 1) { hashEncoder.matches(password, match { it == user.hashedPassword }) }
        verify(exactly = 0) { jwtService.generateAccessToken(any()) }
        verify(exactly = 0) { jwtService.generateRefreshToken(any()) }
    }

    @Test
    fun `refresh should return new tokens on success`() {
        // given
        val userId = ObjectId.get()
        val oldRefreshToken = "old-refresh-token"
        val newAccessToken = "new-access-token"
        val newRefreshToken = "new-refresh-token"
        val refreshTokenObj = RefreshToken(userId, oldRefreshToken, java.time.Instant.now().plusSeconds(1000000))

        every { jwtService.validateRefreshToken(any()) } returns true
        every { jwtService.getUserIdFromToken(any()) } returns userId.toString()
        every { userRepository.findById(any()) } returns Optional.of(User(email, "hashedPassword", id = userId))

        every { refreshTokenRepository.findByUserRefAndHashedToken(any(), any()) } returns refreshTokenObj
        every { refreshTokenRepository.deleteById(any()) } just Runs
        every { jwtService.generateAccessToken(any()) } returns newAccessToken
        every { jwtService.generateRefreshToken(any()) } returns newRefreshToken
        every { refreshTokenRepository.save(any()) } returns mockk()
        every { jwtService.refreshTokenValidityMs } returns 1000000L

        // when
        val tokenPair = authService.refresh(oldRefreshToken)

        // then
        assertNotNull(tokenPair)
        assertEquals(newAccessToken, tokenPair.accessToken)
        assertEquals(newRefreshToken, tokenPair.refreshToken )
        verify { refreshTokenRepository.deleteById(refreshTokenObj.id) }
        verify { refreshTokenRepository.save(match { it.userRef == userId && StringUtils.hasText(it.hashedToken) }) }
    }

    @Test
    fun `refresh should throw when token invalid`() {
        // given
        every { jwtService.validateRefreshToken(any()) } returns false

        // when + then
        val exception = assertThrows<ResponseStatusException> {
            authService.refresh("refresh-token")
        }
        assertEquals(HttpStatus.UNAUTHORIZED, exception.statusCode)
        assertEquals("Invalid refresh token", exception.reason)
        verify(exactly = 0) { refreshTokenRepository.deleteById(any()) }
        verify(exactly = 0) { refreshTokenRepository.save(any()) }
    }

    @Test
    fun `refresh should throw when user not found`() {
        // given
        val userId = ObjectId.get()
        val oldRefreshToken = "old-refresh-token"
        every { jwtService.validateRefreshToken(any()) } returns true
        every { jwtService.getUserIdFromToken(any()) } returns userId.toString()
        every { userRepository.findById(any()) } returns Optional.empty()

        // when + then
        val exception = assertThrows<ResponseStatusException> {
            authService.refresh(oldRefreshToken)
        }
        assertEquals(HttpStatus.UNAUTHORIZED, exception.statusCode)
        assertEquals("Invalid refresh token", exception.reason)
        verify(exactly = 0) { refreshTokenRepository.deleteById(any()) }
        verify(exactly = 0) { refreshTokenRepository.save(any()) }
    }
}