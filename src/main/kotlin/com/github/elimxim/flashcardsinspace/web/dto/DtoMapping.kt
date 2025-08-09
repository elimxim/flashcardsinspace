package com.github.elimxim.flashcardsinspace.web.dto

import com.github.elimxim.flashcardsinspace.entity.Flashcard
import com.github.elimxim.flashcardsinspace.entity.FlashcardSet
import com.github.elimxim.flashcardsinspace.entity.FlashcardTimeline
import com.github.elimxim.flashcardsinspace.entity.Language
import com.github.elimxim.flashcardsinspace.entity.ReviewHistory
import com.github.elimxim.flashcardsinspace.entity.ReviewInfo
import com.github.elimxim.flashcardsinspace.entity.User

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
    languageId = language.id,
    default = first,
    createdAt = creationDate.toString(),
    lastUpdatedAt = lastUpdatedAt?.toString(),
)

fun Flashcard.toDto() = FlashcardDto(
    id = id,
    frontSide = frontSide,
    backSide = backSide,
    stage = stage.name,
    reviewCount = timesReviewed,
    reviewHistory = reviewHistory.toDto(),
    createdAt = creationDate.toString(),
    reviewedAt = lastReviewDate?.toString(),
    lastUpdatedAt = lastUpdatedAt?.toString(),
)

fun ReviewHistory.toDto() = ReviewHistoryDto(
    history = history.map { it.toDto() }
)

fun ReviewInfo.toDto() = ReviewInfoDto(
    stage = stage.name,
    reviewedAt = reviewDate.toString(),
)

fun FlashcardTimeline.toDto() = TimelineDto(
    id = id,
    startedAt = startedAt.toString(),
    status = status.name,
    lastUpdatedAt = lastUpdatedAt?.toString()
)