import { type Flashcard } from '@/model/flashcard.ts'
import { type Stage, getStage, stages, specialStages } from '@/core-logic/stage-logic.ts'
import { useCalendarStore } from '@/stores/calendar-store.ts'
import type { LightDay } from '@/model/calendar.ts'
import { storeToRefs } from 'pinia'

export function findFlashcardsForReview(flashcards: Flashcard[]): Flashcard[] {
  const calendarStore = useCalendarStore()
  const currLightDay = calendarStore.currLightDay
  const currLightDayStages = calendarStore.currLightDayStages

  return flashcards.filter(f => {
    const isStageAvailable = currLightDayStages.has(f.stage)
    if (f.stage === stages.S1.name) {
      return isStageAvailable
        && !isUnknownFlashcard(f, currLightDay)
        && !isReviewedFlashcard(f, currLightDay)
    }
    return isStageAvailable && !isReviewedFlashcard(f, currLightDay)
  }).sort((a, b) => {
    if (a.stage !== b.stage) {
      return getStage(b.stage).order - getStage(a.stage).order
    }
    return a.id - b.id
  })
}

function isUnknownFlashcard(flashcard: Flashcard, lightDay: LightDay): boolean {
  return flashcard.createdAt === lightDay.isoDate
}

function isReviewedFlashcard(flashcard: Flashcard, lightDay: LightDay): boolean {
  return flashcard.reviewedAt === lightDay.isoDate
}

export function flashcardsForStage(flashcards: Flashcard[], stage: Stage): Flashcard[] {
  const calendarStore = useCalendarStore()
  const { currLightDay } = storeToRefs(calendarStore)

  if (stage === specialStages.UNKNOWN) {
    return flashcards.filter(f =>
      f.stage === stages.S1.name && isUnknownFlashcard(f, currLightDay.value)
    ).sort((a, b) =>
      a.id - b.id
    )
  } else if (stage === specialStages.ATTEMPTED) {
    return flashcards.filter(f =>
      f.stage === stages.S1.name && isReviewedFlashcard(f, currLightDay.value)
    ).sort((a, b) =>
      a.id - b.id
    )
  }

  return flashcards.filter(f => f.stage === stage.name)
    .sort((a, b) => a.id - b.id)
}

export function countFlashcards(flashcards: Flashcard[], stage: Stage): number {
  const calendarStore = useCalendarStore()
  const { currLightDay } = storeToRefs(calendarStore)

  if (stage === specialStages.UNKNOWN) {
    return flashcards.filter(f =>
      f.stage === stages.S1.name && isUnknownFlashcard(f, currLightDay.value)
    ).length
  } else if (stage === specialStages.ATTEMPTED) {
    return flashcards.filter(f =>
      f.stage === stages.S1.name && isReviewedFlashcard(f, currLightDay.value)
    ).length
  } else if (stage == stages.S1) {
    return flashcards.filter(f =>
      f.stage === stages.S1.name && !isUnknownFlashcard(f, currLightDay.value) && !isReviewedFlashcard(f, currLightDay.value)
    ).length
  } else {
    return flashcards.filter(f => f.stage === stage.name).length
  }
}
