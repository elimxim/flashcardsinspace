import { type Flashcard } from '@/models/flashcard.ts'
import { type Stage, getStage, stages, specialStages } from '@/core-logic/stage-logic.ts'
import { useCalendarDataStore } from '@/stores/calendar-data.ts'
import type { LightDay } from '@/models/calendar.ts'
import { storeToRefs } from 'pinia'

export function findFlashcardsForReview(flashcards: Flashcard[]): Flashcard[] {
  const calendarDataStore = useCalendarDataStore()
  const currLightDay = calendarDataStore.currLightDay
  const currLightDayStages = calendarDataStore.currLightDayStages

  return flashcards.filter(f => {
    const isStageAvailable = currLightDayStages.has(f.stage)
    if (f.stage === stages.FIRST.name) {
      return isStageAvailable
        && !isUnknownFlashcard(f, currLightDay)
        && !isReviewedFlashcard(f, currLightDay)
    }
    return isStageAvailable && !isReviewedFlashcard(f, currLightDay)
  })
    .sort((a, b) => {
      if (a.stage !== b.stage) {
        return getStage(b.stage).order - getStage(a.stage).order
      }
      return b.id - a.id
    })
}

function isUnknownFlashcard(flashcard: Flashcard, lightDay: LightDay): boolean {
  return flashcard.createdAt === lightDay.isoDate
}

function isReviewedFlashcard(flashcard: Flashcard, lightDay: LightDay): boolean {
  return flashcard.reviewedAt === lightDay.isoDate
}

export function flashcardsForStage(flashcards: Flashcard[], stage: Stage): Flashcard[] {
  const calendarDataStore = useCalendarDataStore()
  const { currLightDay } = storeToRefs(calendarDataStore)

  if (stage === specialStages.UNKNOWN) {
    return flashcards.filter(f =>
      f.stage === stages.FIRST.name && isUnknownFlashcard(f, currLightDay.value)
    )
  } else if (stage === specialStages.ATTEMPTED) {
    console.log('ATTEMPTED')
    return flashcards.filter(f =>
      f.stage === stages.FIRST.name && isReviewedFlashcard(f, currLightDay.value)
    )
  }

  return flashcards.filter(f => f.stage === stage.name)
}

export function countFlashcards(flashcards: Flashcard[], stage: Stage): number {
  const calendarDataStore = useCalendarDataStore()
  const { currLightDay } = storeToRefs(calendarDataStore)

  if (stage === specialStages.UNKNOWN) {
    return flashcards.filter(f =>
      f.stage === stages.FIRST.name && isUnknownFlashcard(f, currLightDay.value)
    ).length
  } else if (stage === specialStages.ATTEMPTED) {
    return flashcards.filter(f =>
      f.stage === stages.FIRST.name && isReviewedFlashcard(f, currLightDay.value)
    ).length
  } else if (stage == stages.FIRST) {
    return flashcards.filter(f =>
      f.stage === stages.FIRST.name && !isUnknownFlashcard(f, currLightDay.value) && !isReviewedFlashcard(f, currLightDay.value)
    ).length
  } else {
    return flashcards.filter(f => f.stage === stage.name).length
  }
}
