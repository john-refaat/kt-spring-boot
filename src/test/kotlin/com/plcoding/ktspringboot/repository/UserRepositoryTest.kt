package com.plcoding.ktspringboot.repository

import com.plcoding.ktspringboot.model.User
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.test.context.ActiveProfiles

private const val test_email = "test@example.com"

@DataMongoTest
@ActiveProfiles("test")
class UserRepositoryTest @Autowired constructor(
    val userRepository: UserRepository
) {

    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()
    }

    @Test
    fun findByEmail() {
        userRepository.save(User(email = test_email, hashedPassword = "viunklnxqjwdpoqw", id = ObjectId.get()))
        val found = userRepository.findByEmail(test_email)
        assertNotNull(found)
        assertEquals(test_email, found.email)
    }

    @Test
    fun findById() {
        val userId = ObjectId.get()
        userRepository.save(User(email = test_email, hashedPassword = "", id = userId))
        val found = userRepository.findById(userId)
        assertNotNull(found)
        assertEquals(userId, found.get().id)
    }
}