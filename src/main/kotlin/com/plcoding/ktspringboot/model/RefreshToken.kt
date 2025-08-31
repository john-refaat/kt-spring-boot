package com.plcoding.ktspringboot.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("refresh_tokens")
data class RefreshToken(
    val userRef: ObjectId,
    val hashedToken: String,

    @Indexed(expireAfter = "0s")
    val expiresAt: Instant,

    val issuedAt: Instant = Instant.now(),

    @Id val id: ObjectId = ObjectId()
)
