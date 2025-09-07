package com.plcoding.ktspringboot.repository

import com.plcoding.ktspringboot.model.User
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@DataMongoTest
class UserRepositoryContainerTest @Autowired constructor(
    val userRepository: UserRepository
): MongoDbContainerTest() {

    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()
    }

    @Test
    fun `test user repository`() {
        assert(userRepository.findAll().isEmpty())
    }

    @Test
    fun `test user insertion`() {
        val user = User(email = "test@example.com", hashedPassword = "qwedrtyuilkjhgfewa")
        userRepository.save(user)
        assert(userRepository.findAll().isNotEmpty())
        val foundUser = userRepository.findByEmail(user.email)
        assertNotNull(foundUser)
        Assertions.assertEquals(user.email, foundUser.email)
   }



}