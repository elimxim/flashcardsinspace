package com.github.elimxim.flashcardsinspace.web.exception

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
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
        val body = ErrorResponseBody.from(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            path = request.getDescription(false),
        )

        return ResponseEntity(body, body.status)
    }

    @ExceptionHandler(InternalServerErrorException::class)
    fun handleInternalServerErrorException(e: InternalServerErrorException, request: WebRequest): ResponseEntity<ErrorResponseBody> {
        log.error("INTERNAL_SERVER_ERROR exception occurred", e)
        val body = ErrorResponseBody.from(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            path = request.getDescription(false),
            e = e,
        )

        return ResponseEntity(body, body.status)
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(e: NotFoundException, request: WebRequest): ResponseEntity<ErrorResponseBody> {
        log.info("NOT_FOUND exception occurred: ${e.message}")
        val body = ErrorResponseBody.from(
            status = HttpStatus.NOT_FOUND,
            path = request.getDescription(false),
            e = e,
        )

        return ResponseEntity(body, body.status)
    }

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequestException(e: BadRequestException, request: WebRequest): ResponseEntity<ErrorResponseBody> {
        log.info("BAD_REQUEST exception occurred: ${e.message}")
        val body = ErrorResponseBody.from(
            status = HttpStatus.BAD_REQUEST,
            path = request.getDescription(false),
            e = e,
        )

        return ResponseEntity(body, body.status)
    }
}

data class ErrorResponseBody(
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val timestamp: LocalDateTime,
    @JsonIgnore
    val status: HttpStatus,
    val statusCode: Int,
    val error: String,
    val errorCode: String,
    val path: String,
) {
    companion object {
        fun from(status: HttpStatus, path: String, e: WebException? = null): ErrorResponseBody {
            return ErrorResponseBody(
                timestamp = LocalDateTime.now(),
                status = status,
                statusCode = status.value(),
                error = status.reasonPhrase,
                errorCode = e?.code ?: "#!NA!#",
                path = path,
            )
        }
    }
}