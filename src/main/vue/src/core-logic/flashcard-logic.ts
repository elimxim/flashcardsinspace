import { type Flashcard, type FlashcardSet, type ReviewInfo } from '@/model/flashcard.ts'
import { type Stage, stages } from '@/core-logic/stage-logic.ts'
import type { Language } from '@/model/language.ts'
import { useCalendarStore } from '@/stores/calendar.ts'
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
  flashcard.reviewHistory.push(info)
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
    language: language,
    flashcardMap: new Map(),
    createdAt: currLightDay.value.isoDate,
    lastUpdatedAt: null,
    default: false,
  }
}

