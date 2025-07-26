import { type Flashcard, type FlashcardSet, type ReviewInfo } from '@/models/flashcard.ts'
import { type Stage, stages } from '@/core-logic/stage-logic.ts'
import type { Language } from '@/models/language.ts'
import { useCalendarDataStore } from '@/stores/calendar-data.ts'
import { storeToRefs } from 'pinia'

/**
 * Creates a new flashcard object.
 * Every newly created flashcard goes to the FIRST stage.
 */
export function newFlashcard(frontSide: string, backSide: string): Flashcard {
  const calendarDateStore = useCalendarDataStore()
  const { currLightDay } = storeToRefs(calendarDateStore)

  return {
    id: 0,
    frontSide: frontSide,
    backSide: backSide,
    stage: stages.FIRST.name,
    reviewedAt: null,
    reviewCount: 0,
    reviewHistory: [],
    createdAt: currLightDay.value.isoDate,
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
  const calendarDateStore = useCalendarDataStore()
  const { currLightDay } = storeToRefs(calendarDateStore)

  const reviewedAt = currLightDay.value.isoDate

  const info: ReviewInfo = {
    stage: stage.name,
    reviewedAt: reviewedAt,
  }

  flashcard.stage = stage.name
  flashcard.reviewedAt = reviewedAt
  flashcard.reviewCount += 1
  flashcard.reviewHistory.push(info)
  flashcard.lastUpdatedAt = new Date().toISOString()
  return flashcard
}

export function newFlashcardSet(name: string, language: Language): FlashcardSet {
  const calendarDateStore = useCalendarDataStore()
  const { currLightDay } = storeToRefs(calendarDateStore)

  return {
    id: 0,
    name: name,
    language: language,
    flashcardMap: new Map(),
    createdAt: currLightDay.value.isoDate,
    lastUpdatedAt: null,
    default: false,
  }
}

