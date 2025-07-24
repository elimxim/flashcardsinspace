import { type Flashcard, type FlashcardSet, type ReviewInfo } from '@/models/flashcard.ts'
import { type Stage, stages } from '@/core-logic/stage-logic.ts'
import type { Language } from '@/models/language.ts';

/**
 * Creates a new flashcard object.
 * Every newly created flashcard goes to the UNKNOWN stage first.
 */
export function newFlashcard(frontSide: string, backSide: string): Flashcard {
  return {
    id: 0,
    frontSide: frontSide,
    backSide: backSide,
    stage: stages.UNKNOWN.name,
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

export function updateFlashcard(flashcard: Flashcard, stage: Stage): Flashcard {
  const now = new Date().toISOString()
  const info: ReviewInfo = {
    stage: stage.name,
    reviewedAt: now,
  }

  flashcard.stage = stage.name
  flashcard.reviewedAt = now
  flashcard.reviewCount += 1
  flashcard.reviewHistory.push(info)
  flashcard.lastUpdatedAt = now
  return flashcard
}

export function newFlashcardSet(name: string, language: Language): FlashcardSet {
  return {
    id: 0,
    name: name,
    language: language,
    flashcardMap: new Map(),
    createdAt: new Date().toISOString(),
    lastUpdatedAt: null,
    default: false,
  }
}

