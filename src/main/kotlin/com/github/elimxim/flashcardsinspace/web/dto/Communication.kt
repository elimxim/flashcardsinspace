package com.github.elimxim.flashcardsinspace.web.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.ZonedDateTime

// todo kotlin way for jakarta annotations?
data class SignUpRequest(
    @field:NotBlank val email: String,
    @field:NotBlank val secret: String,
    @field:NotBlank val name: String,
    @field:NotNull val languageId: Long,
)

data class LoginRequest(
    @field:NotBlank val email: String,
    @field:NotBlank val secret: String
)

// fixme
data class UserResponse(val user: UserDto)
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
