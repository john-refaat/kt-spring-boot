package com.plcoding.ktspringboot.service


import com.plcoding.ktspringboot.model.Note
import com.plcoding.ktspringboot.repository.NoteRepository
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class NoteServiceImpl (
    private val noteRepository: NoteRepository
) : NoteService {

    override fun getByOwnerId(ownerId: String): List<Note> {
        return noteRepository.findByOwnerId(ObjectId(ownerId))
    }

    override fun getByIdAndOwnerId(id: String, ownerId: String): Note {
        return noteRepository.findByIdAndOwnerId(ObjectId(id), ObjectId(ownerId))
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found")
    }

    override fun saveNoteForOwner(note: Note): Note {
        return noteRepository.save(note)
    }

    override fun deleteByIdAndOwnerId(id: String, ownerId: String) {
        noteRepository.findByIdAndOwnerId(ObjectId(id), ObjectId(ownerId))
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found")

        noteRepository.deleteById(ObjectId(id))
    }
}