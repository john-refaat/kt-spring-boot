package com.plcoding.ktspringboot.service

import com.plcoding.ktspringboot.model.Note

interface NoteService {

    fun getByOwnerId(ownerId: String): List<Note>
    fun getByIdAndOwnerId(id: String, ownerId: String): Note
    fun saveNoteForOwner(note: Note): Note
    fun deleteByIdAndOwnerId(id: String, ownerId: String)
}