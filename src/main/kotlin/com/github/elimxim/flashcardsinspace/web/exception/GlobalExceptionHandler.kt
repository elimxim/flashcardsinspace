package com.github.elimxim.flashcardsinspace.web.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.resource.NoResourceFoundException

private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFoundException(ex: NoResourceFoundException, request: WebRequest): ResponseEntity<Any> {
        log.warn("Resource not found for request {}: {}", request.getDescription(false), ex.message)
        return ResponseEntity.notFound().build()
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(ex: Exception, request: WebRequest): ResponseEntity<ExceptionResponseBody> {
        log.error("INTERNAL_SERVER_ERROR exception occurred: ${ex.message}", ex)
        val response = ExceptionResponseBody.from(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            exception = ex,
            path = request.getDescription(false)
        )

        return ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(ex: NotFoundException, request: WebRequest): ResponseEntity<ExceptionResponseBody> {
        log.info("NOT_FOUND exception occurred: ${ex.message}")
        val response = ExceptionResponseBody.from(
            status = HttpStatus.NOT_FOUND,
            exception = ex,
            path = request.getDescription(false)
        )

        return ResponseEntity(response, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequestException(ex: BadRequestException, request: WebRequest): ResponseEntity<ExceptionResponseBody> {
        log.info("BAD_REQUEST exception occurred: ${ex.message}")
        val response = ExceptionResponseBody.from(
            status = HttpStatus.BAD_REQUEST,
            exception = ex,
            path = request.getDescription(false)
        )

        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }
}