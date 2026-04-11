import type { Chronoday, DayStreak } from '@/model/chrono.ts'
import { Flashcard, FlashcardSet } from '@/model/flashcard.ts'

export interface Page<T> {
  content: T[]
  totalPages: number
  last: boolean
}

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
  dayStreak: DayStreak,
}

export interface ChronoBulkUpdateRequest {
  ids: ChronodayId[],
  status: string,
}

export interface ChronoUpdateResponse {
  chronodays: Chronoday[]
  dayStreak: DayStreak,
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
  metadata?: Record<string, unknown>,
}

export interface ReviewSessionUpdateRequest {
  elapsedTime: number,
  flashcardIds?: FlashcardId[],
  finished?: boolean,
  metadata?: Record<string, unknown>,
}

export interface FlashcardId {
  id: number
}

export interface VerificationIntentResponse {
  result: string,
  type?: string,
  attempts?: number,
}
