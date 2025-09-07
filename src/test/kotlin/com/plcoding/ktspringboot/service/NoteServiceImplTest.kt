package com.plcoding.ktspringboot.service

import com.plcoding.ktspringboot.model.Note
import com.plcoding.ktspringboot.repository.NoteRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NoteServiceImplTest {

    private val noteRepository = mockk<NoteRepository>()
    private val noteService = NoteServiceImpl(noteRepository)

    @BeforeEach
    fun setUp() {

    }

    @Test
    fun `test get Notes by owner Id`() {
        val ownerId = ObjectId()
        val notes = listOf(
            Note("title1", "content1", 1L, ownerId, java.time.Instant.now()),
            Note("title2", "content2", 2L, ownerId, java.time.Instant.now())
        )
        every { noteRepository.findByOwnerId(ownerId) } returns notes

        val foundNotes = noteService.getByOwnerId(ownerId.toString())
        assert(foundNotes.size == 2)
        assert(foundNotes.all { it.ownerId == ownerId })
        assert(foundNotes.all { notes.contains(it) })
        verify(exactly = 1) { noteRepository.findByOwnerId(ownerId) }
    }

    @Test
    fun `test get note by id and owner id`() {
        val noteId = ObjectId()
        val ownerId = ObjectId()
        val note = Note("title", "content", 1L, ownerId = ownerId, java.time.Instant.now(), id = noteId)
        every { noteRepository.findByIdAndOwnerId(noteId, ownerId) } returns note
        val foundNote = noteService.getByIdAndOwnerId(noteId.toString(), ownerId.toString())
        assert(foundNote == note)
        verify(exactly = 1) { noteRepository.findByIdAndOwnerId(noteId, ownerId) }
    }

    @Test
    fun `test save note for owner`() {
        val note = Note("title", "content", 1L, ownerId = ObjectId(), java.time.Instant.now())
        every { noteRepository.save(note) } returns note
        val savedNote = noteService.saveNoteForOwner(note)
        assert(savedNote == note)
        verify(exactly = 1) { noteRepository.save(note) }
    }
    
    
    @Test
    fun `test delete note by id and owner id`() {
        val noteId = ObjectId()
        val ownerId = ObjectId()
        val findSlot = slot<ObjectId>()
        val ownerSlot = slot<ObjectId>()
        val idSlot = slot<ObjectId>()
        every { noteRepository.findByIdAndOwnerId(capture(findSlot), capture(ownerSlot)) } returns Note(
            "title",
            "content",
            1L,
            ownerId = ownerId,
            java.time.Instant.now(),
        )
        every { noteRepository.deleteById(capture(idSlot)) } returns Unit
        noteService.deleteByIdAndOwnerId(noteId.toString(), ownerId.toString())
        verify(exactly = 1) { noteRepository.findByIdAndOwnerId(noteId, ownerId) }
        verify(exactly = 1) { noteRepository.deleteById(noteId) }
        assert(findSlot.captured == noteId)
        assert(ownerSlot.captured == ownerId)
        assert(idSlot.captured == noteId)
    }
}