package com.github.elimxim.flashcardsinspace.web.exception

import com.fasterxml.jackson.annotation.JsonFormat
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.resource.NoResourceFoundException
import java.time.LocalDateTime

private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFoundException(e: NoResourceFoundException, request: WebRequest): ResponseEntity<Any> {
        log.warn("Resource not found for request {}: {}", request.getDescription(false), e.message)
        return ResponseEntity.notFound().build()
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(ex: Exception, request: WebRequest): ResponseEntity<ErrorResponseBody> {
        log.error("UNEXPECTED exception occurred", ex)
        val response = ErrorResponseBody.from(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            path = request.getDescription(false),
        )

        return ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(InternalServerErrorException::class)
    fun handleInternalServerErrorException(e: InternalServerErrorException, request: WebRequest): ResponseEntity<ErrorResponseBody> {
        log.error("INTERNAL_SERVER_ERROR exception occurred", e)
        val response = ErrorResponseBody.from(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            path = request.getDescription(false),
            e = e,
        )

        return ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(e: NotFoundException, request: WebRequest): ResponseEntity<ErrorResponseBody> {
        log.info("NOT_FOUND exception occurred: ${e.message}")
        val response = ErrorResponseBody.from(
            status = HttpStatus.NOT_FOUND,
            path = request.getDescription(false),
            e = e,
        )

        return ResponseEntity(response, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequestException(ex: BadRequestException, request: WebRequest): ResponseEntity<ErrorResponseBody> {
        log.info("BAD_REQUEST exception occurred: ${ex.message}")
        val response = ErrorResponseBody.from(
            status = HttpStatus.BAD_REQUEST,
            path = request.getDescription(false),
            e = ex,
        )

        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }
}

data class ErrorResponseBody(
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
) {
    companion object {
        fun from(status: HttpStatus, path: String, e: Exception? = null): ErrorResponseBody {
            return ErrorResponseBody(
                timestamp = LocalDateTime.now(),
                status = status.value(),
                error = status.reasonPhrase,
                message = e?.message ?: "N/A",
                path = path,
            )
        }
    }
}