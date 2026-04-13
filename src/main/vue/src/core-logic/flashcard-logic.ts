import {
  type Flashcard,
  FlashcardContent,
  type FlashcardSet,
  FlashcardSetExtra,
  type ReviewInfo
} from '@/model/flashcard.ts'
import { learningStages, type Stage } from '@/core-logic/stage-logic.ts'
import type { Language } from '@/model/language.ts'

export const flashcardSetStatuses = {
  ACTIVE: 'ACTIVE',
  DELETED: 'DELETED',
  SUSPENDED: 'SUSPENDED',
}

export const flashcardSides = {
  FRONT: 'FRONT',
  BACK: 'BACK',
}

export function newFlashcard(frontSide: string, backSide: string, chronodate: string): Flashcard {
  return {
    id: 0,
    frontSide: frontSide,
    backSide: backSide,
    stage: learningStages.S1.name,
    timesReviewed: 0,
    reviewHistory: { history: [] },
    creationDate: chronodate,
  }
}

export function changeFlashcardSides(flashcard: Flashcard, frontSide: string, backSide: string): boolean {
  let changed = false
  if (flashcard.frontSide !== frontSide) {
    flashcard.frontSide = frontSide
    changed = true
  }
  if (flashcard.backSide !== backSide) {
    flashcard.backSide = backSide
    changed = true
  }

  if (changed) {
    flashcard.lastUpdatedAt = new Date()
  }

  return changed
}

export function copyFlashcardSet(value: FlashcardSet): FlashcardSet {
  return JSON.parse(JSON.stringify(value))
}

export function copyFlashcard(value: Flashcard): Flashcard {
  return JSON.parse(JSON.stringify(value))
}

export function sortFlashcardSets(flashcardSets: FlashcardSet[]): FlashcardSet[] {
  return flashcardSets.sort((a, b) => {
    return a.name.localeCompare(b.name)
  })
}

export function updateFlashcard(flashcard: Flashcard, stage: Stage, chronodate: string): Flashcard {
  const info: ReviewInfo = {
    stage: stage.name,
    reviewDate: chronodate,
  }

  flashcard.stage = stage.name
  flashcard.lastReviewDate = chronodate
  flashcard.timesReviewed += 1
  flashcard.reviewHistory.history.push(info)
  flashcard.lastUpdatedAt = new Date()
  return flashcard
}

export function createFlashcardSet(
  name: string,
  language: Language,
): FlashcardSet {
  return {
    id: 0,
    name: name,
    status: flashcardSetStatuses.ACTIVE,
    languageId: language.id,
    createdAt: new Date(),
  }
}

export function getFlashcardSide(isFrontSide: boolean) {
  return isFrontSide ? flashcardSides.FRONT : flashcardSides.BACK
}

export function mapFlashcardSetExtra(flashcardSetExtras: FlashcardSetExtra[]): Map<number, FlashcardSetExtra> {
  return new Map(flashcardSetExtras.map(v => [v.id, v]))
}

export function findDuplicates(candidates: FlashcardContent[], existing: Flashcard[]): Set<number> {
  const existingSet = new Set(
    existing.map(f => `${f.frontSide.trim().toLowerCase()}::${f.backSide.trim().toLowerCase()}`)
  )
  const duplicates = new Set<number>()
  const seen = new Set<string>()
  for (let i = 0; i < candidates.length; i++) {
    const key = `${candidates[i].frontSide.toLowerCase()}::${candidates[i].backSide.toLowerCase()}`
    if (existingSet.has(key) || seen.has(key)) {
      duplicates.add(i)
    } else {
      seen.add(key)
    }
  }
  return duplicates
}
