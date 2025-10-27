export interface ReviewHistory {
  history: ReviewInfo[]
}

export interface ReviewInfo {
  stage: string
  reviewDate: string // "yyyy-MM-dd"
}

export interface Flashcard {
  id: number
  frontSide: string
  frontSideAudioId?: number | undefined
  backSide: string
  backSideAudioId?: number | undefined
  stage: string
  timesReviewed: number
  reviewHistory: ReviewHistory
  creationDate: string // "yyyy-MM-dd"
  lastReviewDate?: string | undefined // "yyyy-MM-dd"
  lastUpdatedAt?: Date | undefined // ZonedDateTime
}

export interface FlashcardAudio {
  id: number
  side: string
  mimeType: string
  audioSize: number
  uploadedAt?: Date | undefined // ZonedDateTime
}

export interface FlashcardSet {
  id: number
  name: string
  status: string
  languageId: number
  createdAt: Date // ZonedDateTime
  startedAt?: Date | undefined // ZonedDateTime
  lastUpdatedAt?: Date | undefined // ZonedDateTime
}

export interface FlashcardSetExtra {
  id: number
  flashcardsNumber: number
}
