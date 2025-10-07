import { type Flashcard, type FlashcardSet, type ReviewInfo } from '@/model/flashcard.ts'
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
    reviewedAt: null,
    reviewCount: 0,
    reviewHistory: { history: [] },
    createdAt: currDay.value.chronodate,
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
  const chronoStore = useChronoStore()
  const { currDay } = storeToRefs(chronoStore)

  const reviewedAt = currDay.value.chronodate

  const info: ReviewInfo = {
    stage: stage.name,
    reviewedAt: reviewedAt,
  }

  flashcard.stage = stage.name
  flashcard.reviewedAt = reviewedAt
  flashcard.reviewCount += 1
  flashcard.reviewHistory.history.push(info)
  flashcard.lastUpdatedAt = new Date().toISOString()
  return flashcard
}

export function createFlashcardSet(
  name: string,
  language: Language,
  first: boolean = false,
): FlashcardSet {
  return {
    id: 0,
    name: name,
    status: flashcardSetStatuses.ACTIVE,
    default: first,
    languageId: language.id,
    createdAt: new Date(),
    startedAt: null,
    lastUpdatedAt: null,
  }
}
