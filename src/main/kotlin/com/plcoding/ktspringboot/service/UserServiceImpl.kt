package com.plcoding.ktspringboot.service

import com.plcoding.ktspringboot.model.User
import com.plcoding.ktspringboot.repository.UserRepository
import org.bson.types.ObjectId
import kotlin.jvm.optionals.getOrNull

class UserServiceImpl(
    private val userRepository: UserRepository
) : UserService {

    override fun getUserByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }

    override fun getUserById(id: String): User? {
        return userRepository.findById(ObjectId(id))
            .getOrNull()
    }
}