package com.plcoding.ktspringboot.repository

import com.plcoding.ktspringboot.model.RefreshToken
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface RefreshTokenRepository: MongoRepository<RefreshToken, ObjectId> {
    fun findByUserRefAndHashedToken(userRef: ObjectId, hashedToken: String): RefreshToken?
    fun deleteByUserRef(userRef: ObjectId)

}