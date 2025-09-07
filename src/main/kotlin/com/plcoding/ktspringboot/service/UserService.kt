package com.plcoding.ktspringboot.service

import com.plcoding.ktspringboot.model.User

interface UserService {
    fun getUserByEmail(email: String): User?
    fun getUserById(id: String): User?

}