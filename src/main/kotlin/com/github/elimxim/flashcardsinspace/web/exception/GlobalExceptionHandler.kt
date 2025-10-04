package com.github.elimxim.flashcardsinspace.web.exception

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.elimxim.flashcardsinspace.Messages
import com.github.elimxim.flashcardsinspace.security.escapeHtml
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
class GlobalExceptionHandler(private val messages: Messages) {
    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFoundException(e: NoResourceFoundException, request: WebRequest): ResponseEntity<Any> {
        log.warn("Resource not found for request {}: {}", request.getDescription(false), e.message)
        return ResponseEntity.notFound().build()
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(ex: Exception, request: WebRequest): ResponseEntity<ErrorResponseBody> {
        log.error("UNEXPECTED exception occurred", ex)
        val body = errorBody(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            path = request.getDescription(false).escapeHtml(),
            exception = UnexpectedException("Unexpected error occurred", ex)
        )

        return ResponseEntity(body, body.httpStatus)
    }

    @ExceptionHandler(Http5xxException::class)
    fun handle5xxException(e: Http5xxException, request: WebRequest): ResponseEntity<ErrorResponseBody> {
        val httpStatus = getHttpStatus(e)
        log.error("${httpStatus.name} exception occurred", e)
        val body = errorBody(
            status = httpStatus,
            path = request.getDescription(false).escapeHtml(),
            exception = e,
        )

        return ResponseEntity(body, httpStatus)
    }

    @ExceptionHandler(Http4xxException::class)
    fun handle4xxException(e: Http4xxException, request: WebRequest): ResponseEntity<ErrorResponseBody> {
        val httpStatus = getHttpStatus(e)
        log.info("${httpStatus.name} exception occurred: ${e.message}", e.cause)

        val body = errorBody(
            status = httpStatus,
            path = request.getDescription(false).escapeHtml(),
            exception = e,
        )

        return ResponseEntity(body, httpStatus)
    }

    private fun errorBody(
        status: HttpStatus,
        path: String,
        exception: HttpException
    ): ErrorResponseBody {
        return ErrorResponseBody(
            timestamp = LocalDateTime.now(),
            httpStatus = status,
            statusCode = status.value(),
            statusError = status.reasonPhrase,
            errorCode = getErrorCode(exception),
            message = messages.getMessage(exception),
            path = path,
        )
    }

}

data class ErrorResponseBody(
    @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val timestamp: LocalDateTime,
    @field:JsonIgnore
    val httpStatus: HttpStatus,
    val statusCode: Int,
    val statusError: String,
    val errorCode: String,
    val message: String?,
    val path: String,
)
