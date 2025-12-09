package com.github.elimxim.flashcardsinspace.web.dto

import com.github.elimxim.flashcardsinspace.entity.ChronodayStatus
import com.github.elimxim.flashcardsinspace.entity.FlashcardSetStatus
import com.github.elimxim.flashcardsinspace.entity.FlashcardStage
import com.github.elimxim.flashcardsinspace.entity.ReviewSessionType
import com.github.elimxim.flashcardsinspace.security.ConfidentialLength
import com.github.elimxim.flashcardsinspace.security.Password
import com.github.elimxim.flashcardsinspace.security.RequiredConfidential
import com.github.elimxim.flashcardsinspace.service.validation.*
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
    @field:NotNull
    @field:NotBlank
    @field:Size(max = 50)
    @field:ValidTimezone
    var timezone: String? = null,
)

data class ValidSignUpRequest(
    val email: String,
    val name: String,
    val secret: Password,
    val languageId: Long,
    val timezone: String,
)

class LoginRequest(
    @field:NotNull
    @field:NotBlank
    @field:Email
    var email: String? = null,
    @field:RequiredConfidential
    var secret: Password? = null,
    @field:NotNull
    @field:NotBlank
    @field:Size(max = 50)
    @field:ValidTimezone
    var timezone: String? = null,
)

data class ValidLoginRequest(
    val email: String,
    val secret: Password,
    val timezone: String,
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
    var creationDate: String? = null,
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
    var timesReviewed: String? = null,
    @field:Valid
    var reviewHistory: ReviewHistory? = null,
    @field:IsoLocalDate
    var lastReviewDate: String? = null,
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
        var reviewDate: String? = null,
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
    @field:ValidFlashcardSetStatus
    var status: String? = null,
    @field:Pattern(regexp="^\\d+$")
    var languageId: String? = null,
    @field:IsoZonedDateTime
    var startedAt: String? = null,
)

class ValidFlashcardSetUpdateRequest(
    val name: String? = null,
    val status: FlashcardSetStatus? = null,
    val languageId: Long? = null,
    val startedAt: ZonedDateTime? = null,
)

class ChronoSyncRequest(
    @field:NotNull
    @field:NotBlank
    @field:IsoZonedDateTime
    var clientDatetime: String? = null,
)

data class ValidChronoSyncRequest(
    val clientDatetime: ZonedDateTime,
)

data class ChronoSyncResponse(
    val currDay: ChronodayDto,
    val chronodays: List<ChronodayDto>,
    val dayStreak: Int,
)

class ChronoBulkUpdateRequest(
    @field:NotNull
    @field:Size(min = 1)
    @field:Valid
    var ids: List<ChronodayId> = emptyList(),
    @field:NotNull
    @field:ValidChronodayStatus
    var status: String? = null,
) {
    class ChronodayId(
        @field:NotNull
        @field:Pattern(regexp="^\\d+$")
        var id: String? = null,
    )
}

data class ValidChronoBulkUpdateRequest(
    val ids: List<Long>,
    val status: ChronodayStatus,
)

data class ChronoUpdateResponse(
    val chronodays: List<ChronodayDto>,
    val dayStreak: Int,
)

data class FlashcardSetInitResponse(
    val flashcardSet: FlashcardSetDto,
    val flashcard: FlashcardDto,
    val currDay: ChronodayDto,
    val chronodays: List<ChronodayDto>,
)

data class FlashcardSetSuspendResponse(
    val flashcardSet: FlashcardSetDto,
    val currDay: ChronodayDto,
    val chronodays: List<ChronodayDto>,
)

data class UserUpdateRequest(
    @field:NotNull
    @field:NotBlank
    @field:Email
    var email: String? = null,
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

data class ValidUserUpdateRequest(
    val email: String,
    val name: String,
    val languageId: Long,
)

data class ReviewSessionCreateRequest(
    @field:NotNull
    @field:NotBlank
    @field:ValidReviewSessionType
    var type: String? = null,
    @field:NotNull
    @field:NotBlank
    @field:Pattern(regexp="^\\d+$")
    var chronodayId: String? = null,
    @field:Valid
    var flashcardIds: List<FlashcardId>? = null,
) {
    class FlashcardId(
        @field:NotNull
        @field:NotBlank
        @field:Pattern(regexp = "^\\d+$")
        var id: String? = null,
    )
}

data class ValidReviewSessionCreateRequest(
    val type: ReviewSessionType,
    val chronodayId: Long,
    val flashcardIds: Set<Long>,
)

data class ReviewSessionUpdateRequest(
    @field:NotNull
    @field:NotBlank
    @field:Pattern(regexp="^\\d+$")
    var elapsedTime: String? = null,
    @field:Valid
    var flashcardIds: List<FlashcardId>? = null,
    @field:Pattern(regexp="^(true|false)$")
    var finished: String? = null,
    @field:ValidMetadata
    var metadata: Map<String, Any>? = null,
) {
    class FlashcardId(
        @field:NotNull
        @field:NotBlank
        @field:Pattern(regexp = "^\\d+$")
        var id: String? = null,
    )
}

data class ValidReviewSessionUpdateRequest(
    val elapsedTime: Long,
    val flashcardIds: Set<Long>,
    val finished: Boolean,
    val metadata: Map<String, Any>,
)

data class QuizSessionGetResponse(
    val session: ReviewSessionDto,
    val flashcards: List<FlashcardDto>,
    val nextRoundFlashcards: List<FlashcardDto>,
    val reviewedFlashcardIds: List<Long>,
)
