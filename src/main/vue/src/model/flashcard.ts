export interface ReviewHistory {
  history: ReviewInfo[]
}

export interface ReviewInfo {
  stage: string
  reviewedAt: string // fixme reviewDate + Date
}

export interface Flashcard {
  id: number
  frontSide: string
  backSide: string
  stage: string
  reviewCount: number // fixme timesReviewed
  reviewHistory: ReviewHistory
  createdAt: string // fixme creationDate + Date
  reviewedAt: string | null // fixme reviewDate + Date
  lastUpdatedAt: string | null
}

export interface FlashcardSet {
  id: number
  name: string
  languageId: number,
  createdAt: string // fixme lastCreationDate + Date
  default: boolean // fixme first
  lastUpdatedAt: string | null // fixme DAte
}
