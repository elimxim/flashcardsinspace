import type { User } from '@/model/user.ts'
import type { Language } from '@/model/language.ts'
import type { Flashcard, FlashcardSet } from '@/model/flashcard.ts'

// todo UserPostResponse
// todo UserLoginRequest
// todo UserSignUpRequest

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
