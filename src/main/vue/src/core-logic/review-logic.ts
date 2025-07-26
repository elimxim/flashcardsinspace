import { type Flashcard } from '@/models/flashcard.ts'
import { type Stage, getStage, stages } from '@/core-logic/stage-logic.ts'
import { useCalendarDataStore } from '@/stores/calendar-data.ts'
import { storeToRefs } from 'pinia'
import type { LightDay } from '@/models/calendar.ts';

export function findFlashcardsForReview(flashcards: Flashcard[]): Flashcard[] {
  const calendarDataStore = useCalendarDataStore()
  const { currLightDay, currLightDayStages } = storeToRefs(calendarDataStore)

  return flashcards.filter(f => {
    const isStageAvailable = currLightDayStages.value.has(f.stage)
    if (f.stage === stages.FIRST.name) {
      return isStageAvailable
        && !isUnknownFlashcard(f, currLightDay.value)
        && !isAttemptedFlashcard(f, currLightDay.value)
    }
    return isStageAvailable
  })
    .sort((a, b) => {
      if (a.stage !== b.stage) {
        return getStage(a.stage).order - getStage(b.stage).order
      }
      return a.id - b.id
    })
}

function isUnknownFlashcard(flashcard: Flashcard, lightDay: LightDay): boolean {
  return flashcard.createdAt === lightDay.isoDate
}

function isAttemptedFlashcard(flashcard: Flashcard, lightDay: LightDay): boolean {
  return flashcard.reviewedAt === lightDay.isoDate
}

export function flashcardsForStage(flashcards: Flashcard[], stage: Stage): Flashcard[] {
  const calendarDataStore = useCalendarDataStore()
  const { currLightDay } = storeToRefs(calendarDataStore)

  if (stage === stages.UNKNOWN) {
    return flashcards.filter(f =>
      f.stage === stages.FIRST.name && isUnknownFlashcard(f, currLightDay.value)
    )
  } else if (stage === stages.ATTEMPTED) {
    console.log('ATTEMPTED')
    return flashcards.filter(f =>
      f.stage === stages.FIRST.name && isAttemptedFlashcard(f, currLightDay.value)
    )
  }

  return flashcards.filter(f => f.stage === stage.name)
}

export function countFlashcards(flashcards: Flashcard[], stage: Stage): number {
  const calendarDataStore = useCalendarDataStore()
  const { currLightDay } = storeToRefs(calendarDataStore)

  if (stage === stages.UNKNOWN) {
    return flashcards.filter(f =>
      f.stage === stages.FIRST.name && isUnknownFlashcard(f, currLightDay.value)
    ).length
  } else if (stage === stages.ATTEMPTED) {
    return flashcards.filter(f =>
      f.stage === stages.FIRST.name && isAttemptedFlashcard(f, currLightDay.value)
    ).length
  } else if (stage == stages.FIRST) {
    return flashcards.filter(f =>
      f.stage === stages.FIRST.name && !isUnknownFlashcard(f, currLightDay.value) && !isAttemptedFlashcard(f, currLightDay.value)
    ).length
  } else {
    return flashcards.filter(f => f.stage === stage.name).length
  }
}
