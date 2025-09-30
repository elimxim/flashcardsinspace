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
    createdAt = createdAt?.normalize(),
)

fun FlashcardCreationRequest.escapeJava() = FlashcardCreationRequest(
    frontSide = frontSide?.escapeJava(),
    backSide = backSide?.escapeJava(),
    stage = stage?.escapeJava(),
    createdAt = createdAt?.escapeJava(),
)

fun FlashcardUpdateRequest.normalize() = FlashcardUpdateRequest(
    frontSide = frontSide?.normalize(),
    backSide = backSide?.normalize(),
    stage = stage?.normalize(),
    reviewCount = reviewCount?.normalize(),
    reviewHistory = FlashcardUpdateRequest.ReviewHistory(
        history = reviewHistory?.history.orEmpty().map {
            FlashcardUpdateRequest.ReviewInfo(
                stage = it.stage?.normalize(),
                reviewedAt = it.reviewedAt?.normalize(),
            )
        }
    ),
    reviewedAt = reviewedAt?.normalize(),
)

fun FlashcardUpdateRequest.escapeJava() = FlashcardUpdateRequest(
    frontSide = frontSide?.escapeJava(),
    backSide = backSide?.escapeJava(),
    stage = stage?.escapeJava(),
    reviewCount = reviewCount?.escapeJava(),
    reviewHistory = FlashcardUpdateRequest.ReviewHistory(
        history = reviewHistory?.history.orEmpty().map {
            FlashcardUpdateRequest.ReviewInfo(
                stage = it.stage?.escapeJava(),
                reviewedAt = it.reviewedAt?.escapeJava(),
            )
        }
    ),
    reviewedAt = reviewedAt?.escapeJava(),
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
    default = default?.normalize(),
    status = status?.normalize(),
    languageId = languageId?.normalize(),
    startedAt = startedAt?.normalize(),
)

fun FlashcardSetUpdateRequest.escapeJava() = FlashcardSetUpdateRequest(
    name = name?.escapeJava(),
    default = default?.escapeJava(),
    status = status?.escapeJava(),
    languageId = languageId?.escapeJava(),
    startedAt = startedAt?.escapeJava(),
)

