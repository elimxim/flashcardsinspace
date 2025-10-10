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
  backSide: string
  stage: string
  timesReviewed: number
  reviewHistory: ReviewHistory
  creationDate: string // "yyyy-MM-dd"
  lastReviewDate?: string // "yyyy-MM-dd"
  lastUpdatedAt?: Date // ZonedDateTime
}

export interface FlashcardSet {
  id: number
  name: string
  status: string
  languageId: number
  createdAt: Date // ZonedDateTime
  startedAt?: Date // ZonedDateTime
  lastUpdatedAt?: Date // ZonedDateTime
}
