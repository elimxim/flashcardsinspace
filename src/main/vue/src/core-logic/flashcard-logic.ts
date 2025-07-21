import { type Flashcard, type ReviewInfo } from '@/models/flashcard.ts'
import { type Level, levels, nextLevel, specialLevels, getLevel } from '@/core-logic/level-logic.ts'

/**
 * Creates a new flashcard object.
 * Every newly created flashcard goes to the UNKNOWN level first.
 */
export function newFlashcard(frontSide: string, backSide: string): Flashcard {
  return {
    id: 0,
    frontSide: frontSide,
    backSide: backSide,
    level: levels.UNKNOWN.name,
    reviewedAt: null,
    reviewCount: 0,
    reviewHistory: [],
    createdAt: new Date().toISOString(),
    lastUpdatedAt: null,
  }
}

export function updateFlashcardSides(flashcard: Flashcard, frontSide: string, backSide: string): Flashcard {
  flashcard.frontSide = frontSide
  flashcard.backSide = backSide
  flashcard.lastUpdatedAt = new Date().toISOString()
  return flashcard
}

export function updateFlashcard(flashcard: Flashcard, level: Level): Flashcard {
  const now = new Date().toISOString()
  const info: ReviewInfo = {
    level: level.name,
    reviewedAt: now,
  }

  flashcard.level = level.name
  flashcard.reviewedAt = now
  flashcard.reviewCount += 1
  flashcard.reviewHistory.push(info)
  flashcard.lastUpdatedAt = now
  return flashcard
}

