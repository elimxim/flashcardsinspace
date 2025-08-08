import type { User } from '@/model/user.ts'
import type { Language } from '@/model/language.ts'
import type { Flashcard, FlashcardSet } from '@/model/flashcard.ts'
import type { Chronoday, Timeline } from '@/model/timeline.ts'

// todo UserPostResponse
// todo UserLoginRequest
// todo UserSignUpRequest
// todo remove duplicated requests and responses
export interface UserGetResponse {
  user: User
}

export interface LanguageGetResponse {
  languages: Language[]
}

export interface FlashcardSetsGetResponse {
  flashcardSets: FlashcardSet[]
}

export interface FlashcardSetsPostRequest {
  flashcardSet: FlashcardSet,
}

export interface FlashcardSetsPostResponse {
  flashcardSet: FlashcardSet,
}

export interface FlashcardSetPutRequest {
  flashcardSet: FlashcardSet,
}

export interface FlashcardSetPutResponse {
  flashcardSet: FlashcardSet,
}

export interface FlashcardsGetResponse {
  flashcards: Flashcard[]
}

export interface FlashcardsPostRequest {
  flashcard: Flashcard,
}

export interface FlashcardsPostResponse {
  flashcard: Flashcard,
}

export interface FlashcardPutRequest {
  flashcard: Flashcard,
}

export interface FlashcardPutResponse {
  flashcard: Flashcard,
}

export interface FlashcardTimelineResponse {
  timeline: Timeline
}

export interface ChronodaysGetParams {
  clientDatetime: string,
}

export interface ChronodaysGetResponse {
  chronodays: Chronoday[]
}

export interface ChronodaysPostResponse {
  chronoday: Chronoday,
}

export interface ChronodaysPutRequest {
}
