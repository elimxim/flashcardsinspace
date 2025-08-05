import { type Flashcard, type FlashcardSet, type ReviewInfo } from '@/model/flashcard.ts'
import { type Stage, stages } from '@/core-logic/stage-logic.ts'
import type { Language } from '@/model/language.ts'
import { useCalendarStore } from '@/stores/calendar-store.ts'
import { storeToRefs } from 'pinia'

/**
 * Creates a new flashcard object. Every newly created
 * flashcard goes to the FIRST stage. Flashcards are
 * created in the current {@link LightspeedCalendar.currLightDay calendar day}.
 */
export function newFlashcard(frontSide: string, backSide: string): Flashcard {
  const calendarStore = useCalendarStore()
  const { currLightDay } = storeToRefs(calendarStore)

  return {
    id: 0,
    frontSide: frontSide,
    backSide: backSide,
    stage: stages.S1.name,
    reviewedAt: null,
    reviewCount: 0,
    reviewHistory: { history: [] },
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

/**
 * Updates a flashcard object and sets
 * {@link Flashcard.reviewedAt review date} as a date
 * of the current {@link LightspeedCalendar.currLightDay calendar day}.
 */
export function updateFlashcard(flashcard: Flashcard, stage: Stage): Flashcard {
  const calendarStore = useCalendarStore()
  const { currLightDay } = storeToRefs(calendarStore)

  const reviewedAt = currLightDay.value.isoDate

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

/**
 * Create a new flashcard set object. Flashcard sets are
 * created in the current {@link LightspeedCalendar.currLightDay calendar day}.
 */
export function newFlashcardSet(name: string, language: Language): FlashcardSet {
  const calendarStore = useCalendarStore()
  const { currLightDay } = storeToRefs(calendarStore)

  return {
    id: 0,
    name: name,
    languageId: language.id,
    createdAt: currLightDay.value.isoDate,
    lastUpdatedAt: null,
    default: false,
  }
}

