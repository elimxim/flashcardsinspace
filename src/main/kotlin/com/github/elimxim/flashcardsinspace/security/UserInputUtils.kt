package com.github.elimxim.flashcardsinspace.security

import com.github.elimxim.flashcardsinspace.web.dto.*
import org.apache.commons.text.StringEscapeUtils

fun String.escapeHtml(): String {
    return StringEscapeUtils.escapeHtml4(this)
}

fun String.escapeJava(): String {
    return StringEscapeUtils.escapeJava(this)
}

fun String.normalize(): String = this.trim()
fun Secret.normalize() = Secret(this.unmasked().normalize())
fun Secret.escapeJava() = Secret(this.unmasked().escapeJava())
fun Password.normalize() = Password(this.unmasked().normalize())
fun Password.escapeJava() = Password(this.unmasked().escapeJava())

fun SignUpRequest.normalize() = SignUpRequest(
    email = email?.normalize()?.lowercase(),
    name = name?.normalize(),
    secret = secret?.normalize(),
    languageId = languageId?.normalize(),
    timezone = timezone?.normalize(),
)

fun SignUpRequest.escapeJava() = SignUpRequest(
    email = email?.escapeJava(),
    name = name?.escapeJava(),
    secret = secret?.escapeJava(),
    languageId = languageId?.escapeJava(),
    timezone = timezone?.escapeJava(),
)

fun LoginRequest.normalize() = LoginRequest(
    email = email?.normalize()?.lowercase(),
    secret = secret?.normalize(),
    timezone = timezone?.normalize(),
)

fun LoginRequest.escapeJava() = LoginRequest(
    email = email?.escapeJava(),
    secret = secret?.escapeJava(),
    timezone = timezone?.escapeJava(),
)

fun FlashcardCreationRequest.normalize() = FlashcardCreationRequest(
    frontSide = frontSide?.normalize(),
    backSide = backSide?.normalize(),
    stage = stage?.normalize(),
    creationDate = creationDate?.normalize(),
)

fun FlashcardCreationRequest.escapeJava() = FlashcardCreationRequest(
    frontSide = frontSide?.escapeJava(),
    backSide = backSide?.escapeJava(),
    stage = stage?.escapeJava(),
    creationDate = creationDate?.escapeJava(),
)

fun FlashcardUpdateRequest.normalize() = FlashcardUpdateRequest(
    frontSide = frontSide?.normalize(),
    backSide = backSide?.normalize(),
    stage = stage?.normalize(),
    timesReviewed = timesReviewed?.normalize(),
    reviewHistory = FlashcardUpdateRequest.ReviewHistory(
        history = reviewHistory?.history.orEmpty().map {
            FlashcardUpdateRequest.ReviewInfo(
                stage = it.stage?.normalize(),
                reviewDate = it.reviewDate?.normalize(),
            )
        }
    ),
    lastReviewDate = lastReviewDate?.normalize(),
)

fun FlashcardUpdateRequest.escapeJava() = FlashcardUpdateRequest(
    frontSide = frontSide?.escapeJava(),
    backSide = backSide?.escapeJava(),
    stage = stage?.escapeJava(),
    timesReviewed = timesReviewed?.escapeJava(),
    reviewHistory = FlashcardUpdateRequest.ReviewHistory(
        history = reviewHistory?.history.orEmpty().map {
            FlashcardUpdateRequest.ReviewInfo(
                stage = it.stage?.escapeJava(),
                reviewDate = it.reviewDate?.escapeJava(),
            )
        }
    ),
    lastReviewDate = lastReviewDate?.escapeJava(),
)

fun FlashcardSetCreationRequest.normalize() = FlashcardSetCreationRequest(
    name = name?.normalize(),
    languageId = languageId?.normalize(),
)

fun FlashcardSetCreationRequest.escapeJava() = FlashcardSetCreationRequest(
    name = name?.escapeJava(),
    languageId = languageId?.escapeJava(),
)

fun FlashcardSetUpdateRequest.normalize() = FlashcardSetUpdateRequest(
    name = name?.normalize(),
    status = status?.normalize(),
    languageId = languageId?.normalize(),
    startedAt = startedAt?.normalize(),
)

fun FlashcardSetUpdateRequest.escapeJava() = FlashcardSetUpdateRequest(
    name = name?.escapeJava(),
    status = status?.escapeJava(),
    languageId = languageId?.escapeJava(),
    startedAt = startedAt?.escapeJava(),
)

fun ChronoSyncRequest.normalize() = ChronoSyncRequest(
    clientDatetime = clientDatetime?.normalize(),
)

fun ChronoSyncRequest.escapeJava() = ChronoSyncRequest(
    clientDatetime = clientDatetime?.escapeJava(),
)

fun ChronoBulkUpdateRequest.normalize() = ChronoBulkUpdateRequest(
    ids = ids.map { ChronoBulkUpdateRequest.ChronodayId(it.id?.normalize()) },
    status = status?.normalize(),
)

fun ChronoBulkUpdateRequest.escapeJava() = ChronoBulkUpdateRequest(
    ids = ids.map { ChronoBulkUpdateRequest.ChronodayId(it.id?.escapeJava()) },
    status = status?.escapeJava(),
)

fun UserUpdateRequest.normalize() = UserUpdateRequest(
    name = name?.normalize(),
    languageId = languageId?.normalize(),
)

fun UserUpdateRequest.escapeJava() = UserUpdateRequest(
    name = name?.escapeJava(),
    languageId = languageId?.escapeJava(),
)

fun ReviewSessionCreateRequest.normalize() = ReviewSessionCreateRequest(
    type = type?.normalize(),
    chronodayId = chronodayId?.normalize(),
    metadata = metadata?.map { (k, v) -> k.escapeJava() to escapeReviewSessionMetadataValue(v) }?.toMap(),
)

fun ReviewSessionCreateRequest.escapeJava() = ReviewSessionCreateRequest(
    type = type?.escapeJava(),
    chronodayId = chronodayId?.escapeJava(),
    metadata = metadata?.map { (k, v) -> k.escapeJava() to escapeReviewSessionMetadataValue(v) }?.toMap(),
)

fun ReviewSessionUpdateRequest.normalize() = ReviewSessionUpdateRequest(
    elapsedTime = elapsedTime?.normalize(),
    flashcardIds = flashcardIds?.map { ReviewSessionUpdateRequest.FlashcardId(it.id?.normalize()) },
    finished = finished?.normalize(),
    metadata = metadata?.map { (k, v) -> k.escapeJava() to escapeReviewSessionMetadataValue(v) }?.toMap(),
)

fun ReviewSessionUpdateRequest.escapeJava() = ReviewSessionUpdateRequest(
    elapsedTime = elapsedTime?.escapeJava(),
    flashcardIds = flashcardIds?.map { ReviewSessionUpdateRequest.FlashcardId(it.id?.escapeJava()) },
    finished = finished?.escapeJava(),
    metadata = metadata?.map { (k, v) -> k.escapeJava() to escapeReviewSessionMetadataValue(v) }?.toMap(),
)

private fun normalizeReviewSessionMetadataValue(value: Any): Any {
    return when (value) {
        is String -> value.normalize()
        is List<*> -> value.map { v -> v?.let { normalizeReviewSessionMetadataValue(it) } }
        is Map<*, *> -> value.map { (k, v) ->
            (if (k is String) k.normalize() else k) to (if (v != null) normalizeReviewSessionMetadataValue(v) else v)
        }.toMap()
        else -> value
    }
}

private fun escapeReviewSessionMetadataValue(value: Any): Any {
    return when (value) {
        is String -> value.escapeJava()
        is List<*> -> value.map { v -> v?.let { escapeReviewSessionMetadataValue(it) } }
        is Map<*, *> -> value.map { (k, v) ->
            (if (k is String) k.escapeJava() else k) to (v?.let { escapeReviewSessionMetadataValue(it) })
        }.toMap()
        else -> value
    }
}

fun ConfirmationCodeRequest.normalize() = ConfirmationCodeRequest(
    email = email?.normalize(),
    purpose = purpose?.normalize(),
)

fun ConfirmationCodeRequest.escapeJava() = ConfirmationCodeRequest(
    email = email?.escapeJava(),
    purpose = purpose?.escapeJava(),
)

fun VerificationCodeRequest.normalize() = VerificationCodeRequest(
    code = code?.normalize(),
)

fun VerificationCodeRequest.escapeJava() = VerificationCodeRequest(
    code = code?.escapeJava(),
)
