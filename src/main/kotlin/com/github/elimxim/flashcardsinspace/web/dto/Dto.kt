package com.github.elimxim.flashcardsinspace.web.dto

import java.time.LocalDate
import java.time.ZonedDateTime

data class UserDto(
    val id: Long,
    val email: String,
    val emailVerified: Boolean,
    val name: String,
    val roles: List<String>,
    val registeredAt: ZonedDateTime,
    val timezone: String,
    val languageId: Long,
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
    val backSide: String,
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
    val flashcardId: Long,
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

data class ReviewSessionDto(
    val id: Long,
    val type: String,
    val flashcardIds: List<Long>?,
    val elapsedTime: Long,
    val startedAt: ZonedDateTime,
    val finishedAt: ZonedDateTime?,
    val lastUpdatedAt: ZonedDateTime?,
    val metadata: Map<String, Any>?,
)

data class FlashcardAudioMetadataDto(
    val audioId: Long,
    val flashcardSide: String,
    val flashcardId: Long,
)
