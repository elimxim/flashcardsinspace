import type { Chronoday } from '@/model/chrono.ts'
import { Flashcard, FlashcardSet } from '@/model/flashcard.ts'
import { ReviewSession } from '@/model/review.ts'

export interface ErrorResponseBody {
  timestamp: Date
  statusCode: number
  statusError: string,
  errorCode: string,
  message?: string,
}

export interface ChronoSyncRequest {
  clientDatetime: Date,
}

export interface ChronoSyncResponse {
  chronodays: Chronoday[]
  currDay: Chronoday,
  dayStreak: number,
}

export interface ChronoBulkUpdateRequest {
  ids: ChronodayId[],
  status: string,
}

export interface ChronoUpdateResponse {
  chronodays: Chronoday[]
  dayStreak: number,
}

export interface ChronodayId {
  id: number
}

export interface FlashcardSetInitResponse {
  flashcardSet: FlashcardSet,
  flashcard: Flashcard,
  currDay: Chronoday,
  chronodays: Chronoday[],
}

export interface FlashcardSetSuspendResponse {
  flashcardSet: FlashcardSet,
  currDay: Chronoday,
  chronodays: Chronoday[],
}

export interface ReviewSessionCreateRequest {
  type: string,
  chronodayId: number,
  flashcardIds: number[],
}

export interface ReviewSessionUpdateRequest {
  elapsedTime: number,
  flashcardIds?: number[],
  finished?: boolean,
  metadata?: Record<string, unknown>,
}

export interface QuizSessionGetResponse {
  session: ReviewSession,
  flashcards: Flashcard[],
  nextRoundFlashcards: Flashcard[],
  reviewedFlashcardIds: number[],
}
