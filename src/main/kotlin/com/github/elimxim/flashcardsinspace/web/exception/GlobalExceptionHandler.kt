package com.github.elimxim.flashcardsinspace.web.exception

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.elimxim.flashcardsinspace.util.getHttpStatus
import com.github.elimxim.flashcardsinspace.util.printApplicationStackTrace
import com.github.elimxim.flashcardsinspace.util.withLoggingContext
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
    fun handleNoResourceFoundException(e: NoResourceFoundException, request: WebRequest): ResponseEntity<Any> =
        withLoggingContext {
            log.warn("Resource not found for request {}: {}", request.getDescription(false), e.message)
            ResponseEntity.notFound().build()
        }

    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(e: Exception, request: WebRequest): ResponseEntity<ErrorResponseBody> =
        withLoggingContext {
            log.error("UNEXPECTED exception occurred: ${e.message}", e)
            e.printApplicationStackTrace()
            val body = buildErrorBody(
                status = HttpStatus.INTERNAL_SERVER_ERROR,
                exception = HttpInternalServerErrorException(ApiErrorCode.UNE500, "Unexpected error occurred", e)
            )

            ResponseEntity(body, body.httpStatus)
        }

    @ExceptionHandler(HttpException::class)
    fun handleHttpException(e: HttpException, request: WebRequest): ResponseEntity<ErrorResponseBody> =
        withLoggingContext {
            val httpStatus = getHttpStatus(e)
            log.info("${httpStatus.name} exception occurred: ${e.message}", e.cause)
            e.printApplicationStackTrace()
            val body = buildErrorBody(
                status = httpStatus,
                exception = e,
            )

            ResponseEntity(body, httpStatus)
        }

}

private fun buildErrorBody(
    status: HttpStatus,
    exception: HttpException,
): ErrorResponseBody {
    return ErrorResponseBody(
        timestamp = LocalDateTime.now(),
        httpStatus = status,
        statusCode = status.value(),
        statusError = status.reasonPhrase,
        errorCode = exception.apiErrorCode.name,
    )
}

data class ErrorResponseBody(
    @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val timestamp: LocalDateTime,
    @field:JsonIgnore
    val httpStatus: HttpStatus,
    val statusCode: Int,
    val statusError: String,
    val errorCode: String,
)
