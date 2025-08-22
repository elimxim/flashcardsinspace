package com.github.elimxim.flashcardsinspace.web.exception

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

open class NotFoundException(resource: String, by: Any) : RuntimeException("$resource not found by $by")
class FlashcardSetNotFoundException(by: Any) : NotFoundException(resource = "Flashcard Set", by = by)

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
