package com.github.elimxim.flashcardsinspace.web.dto

import java.time.LocalDate
import java.time.ZonedDateTime

data class UserDto(
    val id: Long,
    val email: String,
    val name: String,
    val roles: List<String>,
    val registeredAt: ZonedDateTime,
    val timezone: String,
)

data class LanguageDto(
    val id: Long,
    val name: String,
    val code: String,
)

data class FlashcardSetDto(
    val id: Long,
    val name: String,
    val status: String,
    val languageId: Long,
    val createdAt: ZonedDateTime,
    val startedAt: ZonedDateTime?,
    val lastUpdatedAt: ZonedDateTime?,
)

data class FlashcardSetExtraDto(
    val id: Long,
    val flashcardsNumber: Int,
)

data class FlashcardDto(
    val id: Long,
    val frontSide: String,
    val frontSideAudioId: Long?,
    val backSide: String,
    val backSideAudioId: Long?,
    val stage: String,
    val timesReviewed: Int,
    val reviewHistory: ReviewHistoryDto,
    val creationDate: LocalDate,
    val lastReviewDate: LocalDate?,
    val lastUpdatedAt: ZonedDateTime?,
)

data class FlashcardAudioDto(
    val id: Long,
    val side: String,
    val mimeType: String?,
    val audioSize: Long,
    val uploadedAt: ZonedDateTime?,
)

data class ReviewHistoryDto(
    val history: List<ReviewInfoDto>
)

data class ReviewInfoDto(
    val stage: String,
    val reviewDate: LocalDate,
)

data class ChronodayDto(
    val id: Long,
    val chronodate: LocalDate,
    val seqNumber: Int?,
    val status: String,
    val stages: List<String>,
)
