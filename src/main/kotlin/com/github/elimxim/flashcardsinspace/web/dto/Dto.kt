package com.github.elimxim.flashcardsinspace.web.dto

data class UserDto(
    val id: Long,
    val email: String,
    val name: String,
    val roles: List<String>,
    val registeredAt: String,
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
    val createdAt: String, // fixme creationDate
    val lastUpdatedAt: String?,
)

data class FlashcardDto(
    val id: Long,
    val frontSide: String,
    val backSide: String,
    val stage: String,
    val reviewedTimes: Int,
    val reviewHistory: ReviewHistoryDto,
    val createdAt: String, // fixme creationDate
    val reviewedAt: String?, // fixme reviewDate
    val lastUpdatedAt: String?,
)

data class ReviewHistoryDto(
    val history: List<ReviewInfoDto>
)

data class ReviewInfoDto(
    val stage: String,
    val reviewedAt: String, // fixme reviewDate
)