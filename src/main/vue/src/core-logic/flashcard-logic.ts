import { type Flashcard, type ReviewInfo } from '@/models/flashcard.ts'
import { type Level, levels, nextLevel } from '@/core-logic/level-logic.ts'

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

export function updateFlashcard(flashcard: Flashcard, moveUp: boolean): Flashcard {
  const now = new Date().toISOString()
  const level = determineFlashcardLevel(flashcard, moveUp)
  const info: ReviewInfo = {
    level: level,
    reviewedAt: now,
  }

  flashcard.level = level
  flashcard.reviewedAt = now
  flashcard.reviewCount += 1
  flashcard.reviewHistory.push(info)
  flashcard.lastUpdatedAt = now
  return flashcard
}

/**
 * Determines the new level of a flashcard according
 * to the following criteria:
 * - If the flashcard moves up, it simply moves to the next level;
 * - If the flashcard's in the “FIRST” level or above and needs
 * to be moved down, it moves to the “ATTEMPTED” level;
 * - Flashcards in "UNKNOWN" or "ATTEMPTED" levels cannot move up or down.
 */
function determineFlashcardLevel(flashcard: Flashcard, moveUp: boolean): string {
  if (flashcard.level === levels.UNKNOWN.name || flashcard.level === levels.ATTEMPTED.name) {
    return flashcard.level
  }

  if (moveUp) {
    return nextLevel(flashcard.level).name
  } else {
    return levels.ATTEMPTED.name
  }
}
