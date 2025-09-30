package com.github.elimxim.flashcardsinspace.service.validation

import com.github.elimxim.flashcardsinspace.entity.FlashcardSetStatus
import com.github.elimxim.flashcardsinspace.entity.FlashcardStage
import com.github.elimxim.flashcardsinspace.security.escapeJava
import com.github.elimxim.flashcardsinspace.web.dto.*
import com.github.elimxim.flashcardsinspace.web.exception.InvalidRequestFieldsException
import jakarta.validation.ConstraintViolation
import jakarta.validation.Validator
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Service
class RequestValidator(private val validator: Validator) {
    fun validate(request: LoginRequest): ValidLoginRequest {
        validateRequest<LoginRequest>(request) { violations ->
            throw InvalidRequestFieldsException(
                "Request is invalid ${request.escapeJava()}, violations: $violations",
                fields(violations)
            )
        }

        return request.toValidRequest()
    }

    fun validate(request: SignUpRequest): ValidSignUpRequest {
        validateRequest<SignUpRequest>(request) { violations ->
            throw InvalidRequestFieldsException(
                "Request is invalid ${request.escapeJava()}, violations: $violations",
                fields(violations)
            )
        }

        return request.toValidRequest()
    }

    fun validate(request: FlashcardCreationRequest): ValidFlashcardCreationRequest {
        validateRequest<FlashcardCreationRequest>(request) { violations ->
            throw InvalidRequestFieldsException(
                "Request is invalid ${request.escapeJava()}, violations: $violations",
                fields(violations)
            )
        }

        return request.toValidRequest()
    }

    fun validate(request: FlashcardUpdateRequest): ValidFlashcardUpdateRequest {
        validateRequest<FlashcardUpdateRequest>(request) { violations ->
            throw InvalidRequestFieldsException(
                "Request is invalid ${request.escapeJava()}, violations: $violations",
                fields(violations)
            )
        }

        return request.toValidRequest()
    }

    fun validate(request: FlashcardSetCreationRequest): ValidFlashcardSetCreationRequest {
        validateRequest<FlashcardSetCreationRequest>(request) { violations ->
            throw InvalidRequestFieldsException(
                "Request is invalid ${request.escapeJava()}, violations: $violations",
                fields(violations)
            )
        }

        return request.toValidRequest()
    }

    fun validate(request: FlashcardSetUpdateRequest): ValidFlashcardSetUpdateRequest {
        validateRequest<FlashcardSetUpdateRequest>(request) { violations ->
            throw InvalidRequestFieldsException(
                "Request is invalid ${request.escapeJava()}, violations: $violations",
                fields(violations)
            )
        }

        return request.toValidRequest()
    }

    private inline fun <reified T> validateRequest(request: T, ifInvalid: (List<Violation>) -> Unit) {
        val constraintViolations = validator.validate(request)
        if (constraintViolations.isNotEmpty()) {
            val violations = constraintViolations
                .map { it.toViolation() }
                .toSet()
                .sortedBy { it.field }

            ifInvalid(violations)
        }
    }
}

private data class Violation(
    val field: String?,
    val message: String?,
    val invalidValue: Any?,
)

private fun <T> ConstraintViolation<T>.toViolation(): Violation {
    return Violation(
        field = propertyPath.last().name,
        message = message,
        invalidValue = invalidValue
    )
}

private fun fields(violations: List<Violation>) = violations
    .filter { !it.field.isNullOrBlank() }
    .map { it.field.toString() }
    .distinct()

fun LoginRequest.toValidRequest() = ValidLoginRequest(
    email = email!!,
    secret = secret!!
)

fun SignUpRequest.toValidRequest() = ValidSignUpRequest(
    email = email!!,
    name = name!!,
    secret = secret!!,
    languageId = languageId!!.toLong(),
)

fun FlashcardCreationRequest.toValidRequest() = ValidFlashcardCreationRequest(
    frontSide = frontSide!!,
    backSide = backSide!!,
    stage = FlashcardStage.valueOf(stage!!),
    creationDate = LocalDate.parse(createdAt!!, DateTimeFormatter.ISO_LOCAL_DATE)
)

fun FlashcardUpdateRequest.toValidRequest() = ValidFlashcardUpdateRequest(
    id = id!!.toLong(),
    frontSide = frontSide!!,
    backSide = backSide!!,
    stage = FlashcardStage.valueOf(stage!!),
    timesReviewed = reviewCount!!.toInt(),
    reviewHistory = ValidFlashcardUpdateRequest.ReviewHistory(
        history = reviewHistory!!.history.map {
            ValidFlashcardUpdateRequest.ReviewInfo(
                stage = FlashcardStage.valueOf(it.stage!!),
                reviewDate = LocalDate.parse(it.reviewedAt!!, DateTimeFormatter.ISO_LOCAL_DATE),
            )
        }
    ),
    lastReviewDate = LocalDate.parse(reviewedAt!!, DateTimeFormatter.ISO_LOCAL_DATE),
)

fun FlashcardSetCreationRequest.toValidRequest() = ValidFlashcardSetCreationRequest(
    name = name!!,
    languageId = languageId!!.toLong(),
)

fun FlashcardSetUpdateRequest.toValidRequest() = ValidFlashcardSetUpdateRequest(
    id = id!!.toLong(),
    name = name!!,
    first = default?.toBooleanStrict(),
    status = FlashcardSetStatus.valueOf(status!!),
    languageId = languageId!!.toLong(),
    startedAt = startedAt?.let { ZonedDateTime.parse(it, DateTimeFormatter.ISO_ZONED_DATE_TIME) }
)
