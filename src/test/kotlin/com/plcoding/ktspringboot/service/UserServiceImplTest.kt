package com.plcoding.ktspringboot.service

import com.plcoding.ktspringboot.model.User
import com.plcoding.ktspringboot.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

private const val test_email = "john@test.com"
private const val test_password = "hashed"

class UserServiceImplTest {

    private val repo = mockk<UserRepository>()
    private val userService = UserServiceImpl(repo)


    @Test
    fun getUserByEmail() {
        every { repo.findByEmail(any()) } returns User(
            email = test_email,
            hashedPassword = test_password,
            id = ObjectId.get()
        )
        val result = userService.getUserByEmail(test_email)
        assertEquals(test_email, result?.email)
        assertEquals(test_password, result?.hashedPassword)
        verify(exactly = 1) {
            repo.findByEmail(any())
        }
    }

    @Test
    fun getUserById() {
        every { repo.findById(any()) } returns Optional.of(
            User(test_email, test_password, ObjectId.get())
        )
        val result = userService.getUserById(ObjectId.get().toString())
        assertEquals(test_email, result?.email)
        assertEquals(test_password, result?.hashedPassword)
        verify(exactly = 1) {
            repo.findById(any())
        }
    }
}