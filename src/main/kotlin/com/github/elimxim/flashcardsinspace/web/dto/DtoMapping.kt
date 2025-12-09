package com.github.elimxim.flashcardsinspace.web.dto

import com.github.elimxim.flashcardsinspace.entity.*
import com.github.elimxim.flashcardsinspace.util.getMetadataFieldName

fun User.toDto() = UserDto(
    id = id,
    name = name,
    email = email,
    roles = roles.split(",").toList(),
    registeredAt = registeredAt,
    timezone = timezone,
    languageId = language.id,
)

fun Language.toDto() = LanguageDto(
    id = id,
    name = name,
    code = code,
)

fun FlashcardSet.toDto() = FlashcardSetDto(
    id = id,
    name = name,
    status = status.name,
    languageId = language.id,
    createdAt = createdAt,
    startedAt = startedAt,
    lastUpdatedAt = lastUpdatedAt,
)

fun Flashcard.toDto() = FlashcardDto(
    id = id,
    frontSide = frontSide,
    backSide = backSide,
    stage = stage.name,
    timesReviewed = timesReviewed,
    reviewHistory = reviewHistory.toDto(),
    creationDate = creationDate,
    lastReviewDate = lastReviewDate,
    lastUpdatedAt = lastUpdatedAt,
)

fun ReviewHistory.toDto() = ReviewHistoryDto(
    history = history.map { it.toDto() }
)

fun ReviewInfo.toDto() = ReviewInfoDto(
    stage = stage.name,
    reviewDate = reviewDate,
)

fun FlashcardAudio.toDto() = FlashcardAudioDto(
    id = id,
    side = side.name,
    mimeType = mimeType,
    audioSize = audioSize,
    flashcardId = flashcard.id,
    uploadedAt = uploadedAt,
)

fun ReviewSession.toDto() = ReviewSessionDto(
    id = id,
    type = type.name,
    flashcardIds = flashcardIds?.toList(),
    elapsedTime = elapsedTime,
    startedAt = startedAt,
    finishedAt = finishedAt,
    lastUpdatedAt = lastUpdatedAt,
    metadata = metadata?.toDto(),
)

fun ReviewSessionMetadata.toDto(): Map<String, Any>? = when (this) {
    is QuizMetadata -> {
        buildMap {
            getMetadataFieldName(QuizMetadata::round)?.let {
                put(it, round)
            }
            getMetadataFieldName(QuizMetadata::nextRoundFlashcardIds)?.let {
                put(it, nextRoundFlashcardIds)
            }
            getMetadataFieldName(QuizMetadata::currRoundFlashcardIds)?.let {
                put(it, currRoundFlashcardIds)
            }
            getMetadataFieldName(QuizMetadata::overallCorrectCount)?.let {
                put(it, overallCorrectCount)
            }
            getMetadataFieldName(QuizMetadata::overallTotalCount)?.let {
                put(it, overallTotalCount)
            }
        }
    }
}
