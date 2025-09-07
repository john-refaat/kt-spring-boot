package com.plcoding.ktspringboot.repository

import com.plcoding.ktspringboot.model.Note
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest

@DataMongoTest
class NoteRepositoryTest @Autowired constructor(
    private val noteRepository: NoteRepository
) {

    @BeforeEach
    fun setUp() {
        noteRepository.deleteAll()
    }

    @Test
    fun `test note repository`() {
        assert(noteRepository.findAll().isEmpty())
    }

    @Test
    fun `should find notes by owner id`() {
        val ownerId1 = ObjectId()
        val ownerId2 = ObjectId()

        val note1 = Note("title1", "content1", 1L, ownerId1, java.time.Instant.now())
        val note2 = Note("title2", "content2", 2L, ownerId1, java.time.Instant.now())
        val note3 = Note("title3", "content3", 3L, ownerId2, java.time.Instant.now())

        noteRepository.saveAll(listOf(note1, note2, note3))

        val ownerNotes = noteRepository.findByOwnerId(ownerId1)
        assert(ownerNotes.size == 2)
        assert(ownerNotes.all { it.ownerId == ownerId1 })
    }


}