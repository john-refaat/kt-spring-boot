package com.plcoding.ktspringboot.controllers

import com.plcoding.ktspringboot.model.Note
import com.plcoding.ktspringboot.repository.NoteRepository
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import mu.KotlinLogging
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@RestController
@RequestMapping("/notes")
class NoteController(
    private val repository: NoteRepository
) {
    private val logger = KotlinLogging.logger {}

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

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun save(@Valid @RequestBody request: NoteRequest): NoteResponse {
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        logger.info { "Creating note with title '${request.title}' for user $ownerId" }
        val savedNote = repository.save(Note(
                id = request.id?.let { ObjectId(it) } ?: ObjectId.get(),
                title = request.title,
                content = request.content,
                color = request.color,
                createdAt = Instant.now(),
                ownerId = ObjectId(ownerId)
        ))
        logger.info { "Successfully created note with title '${request.title}'" }
        return savedNote.toResponse()
    }

    @GetMapping
    fun findByOwnerId(): List<NoteResponse> {
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        logger.info { "Retrieving notes for user $ownerId" }
        return repository.findByOwnerId(ObjectId(ownerId))
            .map { it.toResponse() }
    }

    @DeleteMapping("/{id}")
    fun deleteById(@PathVariable id: String) {
        if (!ObjectId.isValid(id)) {
            logger.warn { "Invalid note id format: $id" }
            throw ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid note id format")
        }

        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        logger.info { "Attempting to delete note $id for user $ownerId" }
        val foundNotes = repository.findByIdAndOwnerId(ObjectId(id), ObjectId(ownerId))
        if (foundNotes.isEmpty()) {
            logger.warn { "Note not found: $id" }
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found")
        }
        repository.deleteById(ObjectId(id))
        logger.info { "Successfully deleted note: $id" }
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