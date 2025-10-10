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
)

fun SignUpRequest.escapeJava() = SignUpRequest(
    email = email?.escapeJava(),
    name = name?.escapeJava(),
    secret = secret?.escapeJava(),
    languageId = languageId?.escapeJava(),
)

fun LoginRequest.normalize() = LoginRequest(
    email = email?.normalize()?.lowercase(),
    secret = secret?.normalize(),
)

fun LoginRequest.escapeJava() = LoginRequest(
    email = email?.escapeJava(),
    secret = secret?.escapeJava(),
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
