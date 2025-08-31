package com.plcoding.ktspringboot.controllers

import com.plcoding.ktspringboot.model.Note
import com.plcoding.ktspringboot.repository.NoteRepository
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.bson.types.ObjectId
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/notes")
class NoteController(
    private val repository: NoteRepository
) {

    data class NoteRequest(
        val id: String?,
        @field:NotBlank(message = "Title must not be blank")
        val title: String,
        val content: String,
        val color: Long,
    )

    data class NoteResponse(
        val id: String,
        val title: String,
        val content: String,
        val color: Long,
        val createdAt: Instant
    )

    @PostMapping
    fun save(@Valid @RequestBody request: NoteRequest): NoteResponse {
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        val savedNote = repository.save(Note(
                id = request.id?.let { ObjectId(it) } ?: ObjectId.get(),
                title = request.title,
                content = request.content,
                color = request.color,
                createdAt = Instant.now(),
                ownerId = ObjectId(ownerId)
        ))
        return savedNote.toResponse()
    }

    @GetMapping
    fun findByOwnerId(): List<NoteResponse> {
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        return repository.findByOwnerId(ObjectId(ownerId))
            .map { it.toResponse() }
    }

    @DeleteMapping("/{id}")
    fun deleteById(@PathVariable id: String) {
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        val foundNotes = repository.findByIdAndOwnerId(ObjectId(id), ObjectId(ownerId))
        if (foundNotes.isEmpty())
            throw IllegalArgumentException("Note not found")
        repository.deleteById(ObjectId(id))
    }

    private fun Note.toResponse(): NoteResponse {
        return NoteResponse(
                id = this.id.toHexString(),
                title = this.title,
                content = this.content,
                color = this.color,
                createdAt = this.createdAt
        )
    }

}