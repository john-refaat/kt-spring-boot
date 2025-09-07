package com.plcoding.ktspringboot.repository

import com.plcoding.ktspringboot.model.RefreshToken
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest

@DataMongoTest
class RefreshTokenRepositoryTest @Autowired constructor(
    private val refreshTokenRepository: RefreshTokenRepository
) {

    @BeforeEach
    fun setUp() {
        refreshTokenRepository.deleteAll()
    }

    @Test
    fun `test findByUserRefAndHashedToken`() {
        val testRefreshToken = RefreshToken(
            userRef = ObjectId(),
            hashedToken = "testHashedToken",
            expiresAt = java.time.Instant.now().plus(java.time.Duration.ofDays(1))
        )

        refreshTokenRepository.save(testRefreshToken)

        val foundRefreshToken = refreshTokenRepository.findByUserRefAndHashedToken(
            testRefreshToken.userRef,
            testRefreshToken.hashedToken
        )

        assert(foundRefreshToken != null)
        assert(foundRefreshToken?.hashedToken == testRefreshToken.hashedToken)
    }
    
}