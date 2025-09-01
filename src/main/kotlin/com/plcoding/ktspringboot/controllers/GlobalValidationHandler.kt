package com.plcoding.ktspringboot.controllers

import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalValidationHandler {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        val errors = e.bindingResult.allErrors.map {
            it.defaultMessage ?: "Invalid value"
        }
        logger.warn { "Validation failed: $errors" }
        return ResponseEntity.badRequest().body(mapOf("errors" to errors))
    }

}