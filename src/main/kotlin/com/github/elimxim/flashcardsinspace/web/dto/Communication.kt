package com.github.elimxim.flashcardsinspace.web.dto

import com.github.elimxim.flashcardsinspace.entity.FlashcardSetStatus
import com.github.elimxim.flashcardsinspace.entity.FlashcardStage
import com.github.elimxim.flashcardsinspace.security.ConfidentialLength
import com.github.elimxim.flashcardsinspace.security.Password
import com.github.elimxim.flashcardsinspace.security.RequiredConfidential
import com.github.elimxim.flashcardsinspace.service.validation.IsoLocalDate
import com.github.elimxim.flashcardsinspace.service.validation.IsoZonedDateTime
import com.github.elimxim.flashcardsinspace.service.validation.ValidFlashcardSetStatus
import com.github.elimxim.flashcardsinspace.service.validation.ValidFlashcardStage
import jakarta.validation.Valid
import jakarta.validation.constraints.*
import java.time.LocalDate
import java.time.ZonedDateTime

class SignUpRequest(
    @field:NotNull
    @field:NotBlank
    @field:Email
    var email: String? = null,
    @field:NotNull
    @field:NotBlank
    @field:Size(max = 64)
    @field:Pattern(regexp="^[A-Za-z0-9 _-]+$")
    var name: String? = null,
    @field:RequiredConfidential
    @field:ConfidentialLength(min = 6, max = 64)
    var secret: Password? = null,
    @field:NotNull
    @field:NotBlank
    @field:Pattern(regexp="^\\d+$")
    var languageId: String? = null,
)

data class ValidSignUpRequest(
    val email: String,
    val name: String,
    val secret: Password,
    val languageId: Long,
)

class LoginRequest(
    @field:NotNull
    @field:NotBlank
    @field:Email
    var email: String? = null,
    @field:RequiredConfidential
    var secret: Password? = null,
)

data class ValidLoginRequest(
    val email: String,
    val secret: Password,
)

class FlashcardCreationRequest(
    @field:NotNull
    @field:NotBlank
    @field:Size(max = 512)
    var frontSide: String? = null,
    @field:NotNull
    @field:NotBlank
    @field:Size(max = 512)
    var backSide: String? = null,
    @field:NotNull
    @field:NotBlank
    @field:ValidFlashcardStage
    var stage: String? = null,
    @field:NotNull
    @field:NotBlank
    @field:IsoLocalDate
    var createdAt: String? = null, // fixme creationDate
)

data class ValidFlashcardCreationRequest(
    val frontSide: String,
    val backSide: String,
    val stage: FlashcardStage,
    val creationDate: LocalDate,
)

class FlashcardUpdateRequest(
    @field:Size(max = 512)
    var frontSide: String? = null,
    @field:Size(max = 512)
    var backSide: String? = null,
    @field:ValidFlashcardStage
    var stage: String? = null,
    @field:Pattern(regexp="^\\d+$")
    var reviewCount: String? = null, // fixme timesReviewed
    @field:Valid
    var reviewHistory: ReviewHistory? = null,
    @field:IsoLocalDate
    var reviewedAt: String? = null, // fixme lastReviewDate
) {
    class ReviewHistory(
        @field:Valid
        var history: List<ReviewInfo> = emptyList()
    )

    class ReviewInfo(
        @field:NotNull
        @field:NotBlank
        @field:ValidFlashcardStage
        var stage: String? = null,
        @field:NotNull
        @field:NotBlank
        @field:IsoLocalDate
        var reviewedAt: String? = null, // fixme reviewDate
    )
}

data class ValidFlashcardUpdateRequest(
    val frontSide: String? = null,
    val backSide: String? = null,
    val stage: FlashcardStage? = null,
    val timesReviewed: Int? = null,
    val reviewHistory: ReviewHistory? = null,
    val lastReviewDate: LocalDate? = null,
) {
    data class ReviewHistory(val history: List<ReviewInfo>)
    data class ReviewInfo(val stage: FlashcardStage, val reviewDate: LocalDate)
}

class FlashcardSetCreationRequest(
    @field:NotNull
    @field:NotBlank
    @field:Size(max = 64)
    @field:Pattern(regexp="^[A-Za-z0-9 _-]+$")
    var name: String? = null,
    @field:NotNull
    @field:NotBlank
    @field:Pattern(regexp="^\\d+$")
    var languageId: String? = null,
)

data class ValidFlashcardSetCreationRequest(
    val name: String,
    val languageId: Long,
)

class FlashcardSetUpdateRequest(
    @field:Size(max = 64)
    @field:Pattern(regexp="^[A-Za-z0-9 _-]+$")
    var name: String? = null,
    @field:Pattern(regexp="^(true|false)$")
    var default: String? = null, // fixme first
    @field:ValidFlashcardSetStatus
    var status: String? = null,
    @field:Pattern(regexp="^\\d+$")
    var languageId: String? = null,
    @field:IsoZonedDateTime
    var startedAt: String? = null,
)

class ValidFlashcardSetUpdateRequest(
    val name: String? = null,
    val first: Boolean? = null,
    val status: FlashcardSetStatus? = null,
    val languageId: Long? = null,
    val startedAt: ZonedDateTime? = null,
)

// fixme
data class LanguagesGetResponse(val languages: List<LanguageDto>)
data class ChronodayResponse(val chronoday: ChronodayDto)
data class ChronodaysPutRequest(val clientDatetime: ZonedDateTime)
data class ChronodaysPutResponse(val currDay: ChronodayDto, val chronodays: List<ChronodayDto>)
sealed class RequestBodyContainer {
    data class ChronodayStatus(val status: String) : RequestBodyContainer()
}
