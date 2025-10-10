export interface ReviewHistory {
  history: ReviewInfo[]
}

export interface ReviewInfo {
  stage: string
  reviewDate: string // fixme Date
}

export interface Flashcard {
  id: number
  frontSide: string
  backSide: string
  stage: string
  timesReviewed: number
  reviewHistory: ReviewHistory
  creationDate: string // fixme Date
  lastReviewDate?: string | undefined // fixme Date
  lastUpdatedAt?: string | undefined
}

export interface FlashcardSet {
  id: number
  name: string
  status: string,
  languageId: number
  createdAt: Date
  startedAt?: string | undefined // fixme Date
  lastUpdatedAt?: string | undefined // fixme Date
}
