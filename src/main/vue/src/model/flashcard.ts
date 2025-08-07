import type { Language } from '@/model/language.ts'

export interface ReviewHistory {
  history: ReviewInfo[]
}

export interface ReviewInfo {
  stage: string
  reviewedAt: string // fixme reviewDate
}

export interface Flashcard {
  id: number
  frontSide: string
  backSide: string
  stage: string
  reviewCount: number
  reviewHistory: ReviewHistory
  createdAt: string // fixme creationDate
  reviewedAt: string | null // fixme reviewDate
  lastUpdatedAt: string | null
}

export interface FlashcardSet {
  id: number
  name: string
  languageId: number,
  createdAt: string // fixme lastCreationDate
  default: boolean // fixme first
  lastUpdatedAt: string | null
}
