package com.github.elimxim.flashcardsinspace.web.exception

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

open class NotFoundException(message: String) : Exception(message)
class FlashcardSetNotFoundException(message: String) : NotFoundException(message)

open class BadRequestException(message: String) : Exception(message)
class FlashcardsSetAlreadyInitializedException(message: String) : BadRequestException(message)

class CorruptedChronoStateException(message: String) : Exception(message)

data class ExceptionResponseBody(
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
) {
    companion object {
        fun from(status: HttpStatus, exception: Exception, path: String): ExceptionResponseBody {
            return ExceptionResponseBody(
                status = status.value(),
                error = status.reasonPhrase,
                message = exception.message ?: "An unexpected error occurred",
                path = path,
            )
        }
    }
}
