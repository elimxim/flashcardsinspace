package com.github.elimxim.flashcardsinspace.web.dto

import com.github.elimxim.flashcardsinspace.entity.*

fun User.toDto() = UserDto(
    id = id,
    name = username,
    email = email,
    roles = roles.split(",").toList(),
    registeredAt = registeredAt.toString(),
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
    createdAt = createdAt.toString(),
    startedAt = startedAt?.toString(),
    lastUpdatedAt = lastUpdatedAt?.toString(),
)

fun Flashcard.toDto() = FlashcardDto(
    id = id,
    frontSide = frontSide,
    backSide = backSide,
    stage = stage.name,
    timesReviewed = timesReviewed,
    reviewHistory = reviewHistory.toDto(),
    creationDate = creationDate.toString(),
    lastReviewDate = lastReviewDate?.toString(),
    lastUpdatedAt = lastUpdatedAt?.toString(),
)

fun ReviewHistory.toDto() = ReviewHistoryDto(
    history = history.map { it.toDto() }
)

fun ReviewInfo.toDto() = ReviewInfoDto(
    stage = stage.name,
    reviewDate = reviewDate.toString(),
)
