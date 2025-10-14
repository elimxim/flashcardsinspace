import {
  type Flashcard,
  type FlashcardSet,
  FlashcardSetExtra,
  type ReviewInfo
} from '@/model/flashcard.ts'
import { type Stage, stages } from '@/core-logic/stage-logic.ts'
import type { Language } from '@/model/language.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { storeToRefs } from 'pinia'

export const flashcardSetStatuses = {
  ACTIVE: 'ACTIVE',
  DELETED: 'DELETED',
  SUSPENDED: 'SUSPENDED',
}

export function newFlashcard(frontSide: string, backSide: string): Flashcard {
  const chronoStore = useChronoStore()
  const { currDay } = storeToRefs(chronoStore)

  return {
    id: 0,
    frontSide: frontSide,
    backSide: backSide,
    stage: stages.S1.name,
    timesReviewed: 0,
    reviewHistory: { history: [] },
    creationDate: currDay.value.chronodate,
  }
}

export function updateFlashcardSides(flashcard: Flashcard, frontSide: string, backSide: string): Flashcard {
  flashcard.frontSide = frontSide
  flashcard.backSide = backSide
  flashcard.lastUpdatedAt = new Date()
  return flashcard
}

export function copyFlashcardSet(value: FlashcardSet) : FlashcardSet {
  return JSON.parse(JSON.stringify(value))
}

export function copyFlashcard(value: Flashcard) : Flashcard {
  return JSON.parse(JSON.stringify(value))
}

export function sortFlashcardSets(flashcardSets: FlashcardSet[]): FlashcardSet[] {
  return flashcardSets.sort((a, b) => {
    return a.name.localeCompare(b.name)
  })
}

export function updateFlashcard(flashcard: Flashcard, stage: Stage): Flashcard {
  const chronoStore = useChronoStore()
  const { currDay } = storeToRefs(chronoStore)

  const chronodate = currDay.value.chronodate

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

export function mapFlashcardSetExtra(flashcardSetExtras: FlashcardSetExtra[]): Map<number, FlashcardSetExtra> {
  return new Map(flashcardSetExtras.map(v => [v.id, v]))
}
