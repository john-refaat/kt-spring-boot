package com.plcoding.ktspringboot.repository

import com.plcoding.ktspringboot.model.Note
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface NoteRepository: MongoRepository<Note, ObjectId> {
    fun findByOwnerId(ownerId: ObjectId): List<Note>
    fun findByIdAndOwnerId(id: ObjectId, ownerId: ObjectId): List<Note>

}
