package com.github.elimxim.flashcardsinspace.internal

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.resource.NoResourceFoundException
import java.time.LocalDateTime

private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFoundException(ex: NoResourceFoundException, request: WebRequest): ResponseEntity<Any> {
        logger.warn("Resource not found for request {}: {}", request.getDescription(false), ex.message)
        return ResponseEntity.notFound().build()
    }

    @ExceptionHandler(Exception::class)
    fun handleGlobalException(ex: Exception, request: WebRequest): ResponseEntity<Map<String, Any>> {
        logger.error("An unexpected error occurred: ${ex.message}", ex)

        val body = mapOf(
            "timestamp" to LocalDateTime.now().toString(),
            "status" to HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "error" to "Internal Server Error",
            "message" to (ex.message ?: "An unexpected error occurred"),
            "path" to request.getDescription(false)
        )

        return ResponseEntity(body, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}