import type { Language } from '@/model/language.ts'

export interface ReviewInfo {
  stage: string
  reviewedAt: string
}

export interface Flashcard {
  id: number
  frontSide: string
  backSide: string
  stage: string
  reviewCount: number
  reviewHistory: ReviewInfo[]
  createdAt: string
  reviewedAt: string | null
  lastUpdatedAt: string | null
}

export interface FlashcardSet {
  id: number
  name: string
  language: Language
  flashcardMap: Map<number, Flashcard>
  createdAt: string
  lastUpdatedAt: string | null
  default: boolean
}
