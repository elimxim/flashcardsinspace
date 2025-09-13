package com.github.elimxim.flashcardsinspace.web.dto

import com.github.elimxim.flashcardsinspace.security.ConfidentialLength
import com.github.elimxim.flashcardsinspace.security.Password
import com.github.elimxim.flashcardsinspace.security.RequiredConfidential
import jakarta.validation.constraints.*
import java.time.ZonedDateTime

class SignUpRequest(
    @field:NotNull
    @field:Email
    var email: String?,
    @field:NotNull
    @field:NotBlank
    @field:Size(max = 64)
    @field:Pattern(regexp="^[A-Za-z0-9_-]+$")
    var name: String?,
    @field:NotNull
    @field:RequiredConfidential
    @field:ConfidentialLength(min = 6, max = 64)
    var secret: Password?,
    @field:NotNull
    @field:PositiveOrZero
    var languageId: Long?,
)

data class ValidSignUpRequest(
    val email: String,
    val name: String,
    val secret: Password,
    val languageId: Long,
)

class LoginRequest(
    @field:NotNull
    @field:Email
    var email: String?,
    @field:NotNull
    @field:RequiredConfidential
    var secret: Password?
)

data class ValidLoginRequest(
    val email: String,
    val secret: Password,
)

// fixme
data class LanguagesGetResponse(val languages: List<LanguageDto>)
data class FlashcardSetsGetResponse(val flashcardSets: List<FlashcardSetDto>)
data class FlashcardSetsPostRequest(val flashcardSet: FlashcardSetDto)
data class FlashcardSetsPostResponse(val flashcardSet: FlashcardSetDto)
data class FlashcardSetGetResponse(val flashcardSet: FlashcardSetDto)
data class FlashcardSetPutRequest(val flashcardSet: FlashcardSetDto)
data class FlashcardSetPutResponse(val flashcardSet: FlashcardSetDto)
data class FlashcardsGetResponse(val flashcards: List<FlashcardDto>)
data class FlashcardsPostRequest(val flashcard: FlashcardDto)
data class FlashcardsPostResponse(val flashcard: FlashcardDto)
data class FlashcardPutRequest(val flashcard: FlashcardDto)
data class FlashcardPutResponse(val flashcard: FlashcardDto)
data class ChronodayPutRequest(val chronodayStatus: String)
data class ChronodayResponse(val chronoday: ChronodayDto)
data class ChronodaysPutRequest(val clientDatetime: ZonedDateTime)
data class ChronodaysPutResponse(val currDay: ChronodayDto, val chronodays: List<ChronodayDto>)
// end of fixme

sealed class RequestBodyContainer {
    data class ChronodayStatus(val status: String) : RequestBodyContainer()
}
