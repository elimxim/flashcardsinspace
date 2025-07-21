import type { User } from '@/models/user.ts'
import type { Language } from '@/models/language.ts'

export interface ReviewInfo {
  level: string
  reviewedAt: string
}

export interface Flashcard {
  id: number
  frontSide: string
  backSide: string
  level: string
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
  user: User
}
