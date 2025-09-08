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
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.superclasses

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

        return ResponseEntity(body, body.httpStatus)
    }

    @ExceptionHandler(Http5xxException::class)
    fun handle5xxException(e: Http5xxException, request: WebRequest): ResponseEntity<ErrorResponseBody> {
        val httpStatus = getExceptionHttpStatus(e)
        log.error("${httpStatus.name} exception occurred", e)
        val body = ErrorResponseBody.from(
            status = httpStatus,
            path = request.getDescription(false),
            e = e,
        )

        return ResponseEntity(body, httpStatus)
    }

    @ExceptionHandler(Http4xxException::class)
    fun handle4xxException(e: Http4xxException, request: WebRequest): ResponseEntity<ErrorResponseBody> {
        val httpStatus = getExceptionHttpStatus(e)
        log.info("${httpStatus.name} exception occurred: ${e.message}")
        val body = ErrorResponseBody.from(
            status = httpStatus,
            path = request.getDescription(false),
            e = e,
        )

        return ResponseEntity(body, httpStatus)
    }
}

data class ErrorResponseBody(
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val timestamp: LocalDateTime,
    @JsonIgnore
    val httpStatus: HttpStatus,
    val statusCode: Int,
    val statusError: String,
    val errorId: String,
    val path: String,
) {
    companion object {
        fun from(status: HttpStatus, path: String, e: HttpException? = null): ErrorResponseBody {
            return ErrorResponseBody(
                timestamp = LocalDateTime.now(),
                httpStatus = status,
                statusCode = status.value(),
                statusError = status.reasonPhrase,
                errorId = e?.let { getExceptionId(e) } ?: "#N/A#",
                path = path,
            )
        }
    }
}

private fun getExceptionHttpStatus(e: HttpException): HttpStatus {
    return findAnnotationInCLassHierarchy<ExceptionHttpStatus>(e::class)?.value
        ?: throw IllegalStateException(
            "Couldn't extract exception status from ${e::class.simpleName}"
        )
}

private fun getExceptionId(e: HttpException): String {
    return findAnnotationInCLassHierarchy<ExceptionID>(e::class)?.value
        ?: throw IllegalStateException(
            "Couldn't extract exception id from ${e::class.simpleName}"
        )
}

private inline fun <reified T : Annotation> findAnnotationInCLassHierarchy(kClass: KClass<*>?): T? {
    var currentClass = kClass
    while (currentClass != null) {
        val anno = currentClass.findAnnotation<T>()
        if (anno != null) return anno
        currentClass = currentClass.superclasses.firstOrNull()
    }
    return null
}
