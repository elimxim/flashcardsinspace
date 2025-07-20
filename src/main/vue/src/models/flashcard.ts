import type { User } from '@/models/user.ts'
import type { Language } from '@/models/language.ts'

export enum Level {
  FIRST = 1,
  SECOND,
  THIRD,
  FORTH,
  FIFTH,
  SIXTH,
  SEVENTH,
}

export interface ReviewInfo {
  level: Level
  reviewedAt: string
}

export interface Flashcard {
  id: number
  frontSide: string
  backSide: string
  level: Level
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
