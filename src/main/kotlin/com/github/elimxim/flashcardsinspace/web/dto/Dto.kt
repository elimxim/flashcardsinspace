package com.github.elimxim.flashcardsinspace.web.dto

data class UserDto(
    val id: Long,
    val email: String,
    val name: String,
    val roles: List<String>,
    val registeredAt: String, // fixme ZonedDateTime
)

data class LanguageDto(
    val id: Long,
    val name: String,
    val code: String,
)

data class FlashcardSetDto(
    val id: Long,
    val name: String,
    val languageId: Long,
    val default: Boolean,
    val createdAt: String, // fixme creationDate + LocalDate
    val lastUpdatedAt: String?, // fixme ZonedDateTime
)

data class FlashcardDto(
    val id: Long,
    val frontSide: String,
    val backSide: String,
    val stage: String,
    val reviewCount: Int, // fixme timesReviewed
    val reviewHistory: ReviewHistoryDto,
    val createdAt: String, // fixme creationDate + LocalDate
    val reviewedAt: String?, // fixme lastReviewDate + LocalDate
    val lastUpdatedAt: String?, // fixme ZonedDateTime
)

data class ReviewHistoryDto(
    val history: List<ReviewInfoDto>
)

data class ReviewInfoDto(
    val stage: String,
    val reviewedAt: String, // fixme reviewDate
)

data class TimelineDto(
    val id: Long,
    val startedAt: String, // fixme ZonedDateTime
    val status: String,
    val lastUpdatedAt: String?, // fixme ZonedDateTime
)

data class ChronodayDto(
    val id: Long,
    val chronodate: String, // fixme LocalDate
    val seqNumber: Int,
    val status: String,
    val stages: List<String>,
)