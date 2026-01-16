package com.github.elimxim.flashcardsinspace.service.validation

import com.github.elimxim.flashcardsinspace.entity.*
import com.github.elimxim.flashcardsinspace.security.escapeJava
import com.github.elimxim.flashcardsinspace.web.dto.*
import com.github.elimxim.flashcardsinspace.web.exception.HttpInvalidRequestFieldsException
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
            throw HttpInvalidRequestFieldsException(
                fields(violations),
                "Request is invalid ${request.escapeJava()}, violations: $violations",
            )
        }

        return request.toValidRequest()
    }

    fun validate(request: SignUpRequest): ValidSignUpRequest {
        validateRequest<SignUpRequest>(request) { violations ->
            throw HttpInvalidRequestFieldsException(
                fields(violations),
                "Request is invalid ${request.escapeJava()}, violations: $violations",
            )
        }

        return request.toValidRequest()
    }

    fun validate(request: FlashcardCreationRequest): ValidFlashcardCreationRequest {
        validateRequest<FlashcardCreationRequest>(request) { violations ->
            throw HttpInvalidRequestFieldsException(
                fields(violations),
                "Request is invalid ${request.escapeJava()}, violations: $violations",
            )
        }

        return request.toValidRequest()
    }

    fun validate(request: FlashcardUpdateRequest): ValidFlashcardUpdateRequest {
        validateRequest<FlashcardUpdateRequest>(request) { violations ->
            throw HttpInvalidRequestFieldsException(
                fields(violations),
                "Request is invalid ${request.escapeJava()}, violations: $violations",
            )
        }

        return request.toValidRequest()
    }

    fun validate(request: FlashcardSetCreationRequest): ValidFlashcardSetCreationRequest {
        validateRequest<FlashcardSetCreationRequest>(request) { violations ->
            throw HttpInvalidRequestFieldsException(
                fields(violations),
                "Request is invalid ${request.escapeJava()}, violations: $violations",
            )
        }

        return request.toValidRequest()
    }

    fun validate(request: FlashcardSetUpdateRequest): ValidFlashcardSetUpdateRequest {
        validateRequest<FlashcardSetUpdateRequest>(request) { violations ->
            throw HttpInvalidRequestFieldsException(
                fields(violations),
                "Request is invalid ${request.escapeJava()}, violations: $violations",
            )
        }

        return request.toValidRequest()
    }

    fun validate(request: ChronoSyncRequest): ValidChronoSyncRequest {
        validateRequest<ChronoSyncRequest>(request) { violations ->
            throw HttpInvalidRequestFieldsException(
                fields(violations),
                "Request is invalid ${request.escapeJava()}, violations: $violations",
            )
        }

        return request.toValidRequest()
    }

    fun validate(request: ChronoBulkUpdateRequest): ValidChronoBulkUpdateRequest {
        validateRequest<ChronoBulkUpdateRequest>(request) { violations ->
            throw HttpInvalidRequestFieldsException(
                fields(violations),
                "Request is invalid ${request.escapeJava()}, violations: $violations",
            )
        }

        return request.toValidRequest()
    }

    fun validate(request: UserUpdateRequest): ValidUserUpdateRequest {
        validateRequest<UserUpdateRequest>(request) { violations ->
            throw HttpInvalidRequestFieldsException(
                fields(violations),
                "Request is invalid ${request.escapeJava()}, violations: $violations",
            )
        }

        return request.toValidRequest()
    }

    fun validate(request: ReviewSessionCreateRequest): ValidReviewSessionCreateRequest {
        validateRequest<ReviewSessionCreateRequest>(request) { violations ->
            throw HttpInvalidRequestFieldsException(
                fields(violations),
                "Request is invalid ${request.escapeJava()}, violations: $violations",
            )
        }

        return request.toValidRequest()
    }

    fun validate(request: ReviewSessionUpdateRequest): ValidReviewSessionUpdateRequest {
        validateRequest<ReviewSessionUpdateRequest>(request) { violations ->
            throw HttpInvalidRequestFieldsException(
                fields(violations),
                "Request is invalid ${request.escapeJava()}, violations: $violations",
            )
        }

        return request.toValidRequest()
    }

    fun validate(request: VerificationIntentRequest): ValidVerificationIntentRequest {
        validateRequest<VerificationIntentRequest>(request) { violations ->
            throw HttpInvalidRequestFieldsException(
                fields(violations),
                "Request is invalid ${request.escapeJava()}, violations: $violations",
            )
        }

        return request.toValidRequest()
    }

    fun validate(request: VerificationCodeRequest): ValidVerificationCodeRequest {
        validateRequest<VerificationCodeRequest>(request) { violations ->
            throw HttpInvalidRequestFieldsException(
                fields(violations),
                "Request is invalid ${request.escapeJava()}, violations: $violations",
            )
        }

        return request.toValidRequest()
    }

    fun validate(request: PasswordResetRequest): ValidPasswordResetRequest {
        validateRequest<PasswordResetRequest>(request) { violations ->
            throw HttpInvalidRequestFieldsException(
                fields(violations),
                "Request is invalid ${request.escapeJava()}, violations: $violations",
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
        invalidValue = invalidValue,
    )
}

private fun fields(violations: List<Violation>) = violations
    .filter { !it.field.isNullOrBlank() }
    .map { it.field.toString() }
    .distinct()

fun LoginRequest.toValidRequest() = ValidLoginRequest(
    email = email!!,
    secret = secret!!,
    timezone = timezone!!,
)

fun SignUpRequest.toValidRequest() = ValidSignUpRequest(
    email = email!!,
    name = name!!,
    secret = secret!!,
    languageId = languageId!!.toLong(),
    timezone = timezone!!,
)

fun FlashcardCreationRequest.toValidRequest() = ValidFlashcardCreationRequest(
    frontSide = frontSide!!,
    backSide = backSide!!,
    stage = FlashcardStage.valueOf(stage!!),
    creationDate = LocalDate.parse(creationDate!!, DateTimeFormatter.ISO_LOCAL_DATE),
)

fun FlashcardUpdateRequest.toValidRequest() = ValidFlashcardUpdateRequest(
    frontSide = frontSide,
    backSide = backSide,
    stage = stage?.let { FlashcardStage.valueOf(it) },
    timesReviewed = timesReviewed?.toInt(),
    reviewHistory = reviewHistory?.let {
        ValidFlashcardUpdateRequest.ReviewHistory(
            history = it.history.map { info ->
                ValidFlashcardUpdateRequest.ReviewInfo(
                    stage = FlashcardStage.valueOf(info.stage!!),
                    reviewDate = LocalDate.parse(info.reviewDate!!, DateTimeFormatter.ISO_LOCAL_DATE),
                )
            }
        )
    },
    lastReviewDate = lastReviewDate?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) },
)

fun FlashcardSetCreationRequest.toValidRequest() = ValidFlashcardSetCreationRequest(
    name = name!!,
    languageId = languageId!!.toLong(),
)

fun FlashcardSetUpdateRequest.toValidRequest() = ValidFlashcardSetUpdateRequest(
    name = name,
    status = status?.let { FlashcardSetStatus.valueOf(it) },
    languageId = languageId?.toLong(),
    startedAt = startedAt?.let { ZonedDateTime.parse(it, DateTimeFormatter.ISO_ZONED_DATE_TIME) },
)

fun ChronoSyncRequest.toValidRequest() = ValidChronoSyncRequest(
    clientDatetime = ZonedDateTime.parse(clientDatetime!!, DateTimeFormatter.ISO_ZONED_DATE_TIME),
)

fun ChronoBulkUpdateRequest.toValidRequest() = ValidChronoBulkUpdateRequest(
    ids = ids.map { it.id!!.toLong() },
    status = ChronodayStatus.valueOf(status!!),
)

fun UserUpdateRequest.toValidRequest() = ValidUserUpdateRequest(
    name = name!!,
    languageId = languageId!!.toLong(),
)

fun ReviewSessionCreateRequest.toValidRequest() = ValidReviewSessionCreateRequest(
    type = ReviewSessionType.valueOf(type!!),
    chronodayId = chronodayId!!.toLong(),
    metadata = metadata ?: emptyMap(),
)

fun ReviewSessionUpdateRequest.toValidRequest() = ValidReviewSessionUpdateRequest(
    elapsedTime = elapsedTime!!.toLong(),
    flashcardIds = flashcardIds?.map { it.id!!.toLong() }?.toSet() ?: emptySet(),
    finished = finished?.toBoolean() ?: false,
    metadata = metadata ?: emptyMap(),
)

fun VerificationIntentRequest.toValidRequest() = ValidVerificationIntentRequest(
    email = email!!,
    type = VerificationType.valueOf(type!!),
)

fun VerificationCodeRequest.toValidRequest() = ValidVerificationCodeRequest(
    code = code!!,
)

fun PasswordResetRequest.toValidRequest() = ValidPasswordResetRequest(
    secret = secret!!,
)
