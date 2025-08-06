import { type Flashcard } from '@/model/flashcard.ts'
import { type Stage, getStage, stages, specialStages } from '@/core-logic/stage-logic.ts'
import { useLightspeedScheduleStore } from '@/stores/lightspeed-schedule-store.ts'
import type { LightDay } from '@/model/lightspeed-schedule.ts'
import { storeToRefs } from 'pinia'

export function findFlashcardsForReview(flashcards: Flashcard[]): Flashcard[] {
  const lightspeedScheduleStore = useLightspeedScheduleStore()
  const currDay = lightspeedScheduleStore.currDay
  const currDayStages = lightspeedScheduleStore.currLightDayStages

  return flashcards.filter(f => {
    const isStageAvailable = currDayStages.has(f.stage)
    if (f.stage === stages.S1.name) {
      return isStageAvailable
        && !isUnknownFlashcard(f, currDay)
        && !isReviewedFlashcard(f, currDay)
    }
    return isStageAvailable && !isReviewedFlashcard(f, currDay)
  }).sort((a, b) => {
    if (a.stage !== b.stage) {
      return getStage(b.stage).order - getStage(a.stage).order
    }
    return a.id - b.id
  })
}

function isUnknownFlashcard(flashcard: Flashcard, lightDay: LightDay): boolean {
  return flashcard.createdAt === lightDay.date
}

function isReviewedFlashcard(flashcard: Flashcard, lightDay: LightDay): boolean {
  return flashcard.reviewedAt === lightDay.date
}

export function flashcardsForStage(flashcards: Flashcard[], stage: Stage): Flashcard[] {
  const lightspeedScheduleStore = useLightspeedScheduleStore()
  const { currDay } = storeToRefs(lightspeedScheduleStore)

  if (stage === specialStages.UNKNOWN) {
    return flashcards.filter(f =>
      f.stage === stages.S1.name && isUnknownFlashcard(f, currDay.value)
    ).sort((a, b) =>
      a.id - b.id
    )
  } else if (stage === specialStages.ATTEMPTED) {
    return flashcards.filter(f =>
      f.stage === stages.S1.name && isReviewedFlashcard(f, currDay.value)
    ).sort((a, b) =>
      a.id - b.id
    )
  }

  return flashcards.filter(f => f.stage === stage.name)
    .sort((a, b) => a.id - b.id)
}

export function countFlashcards(flashcards: Flashcard[], stage: Stage): number {
  const lightspeedScheduleStore = useLightspeedScheduleStore()
  const { currDay } = storeToRefs(lightspeedScheduleStore)

  if (stage === specialStages.UNKNOWN) {
    return flashcards.filter(f =>
      f.stage === stages.S1.name && isUnknownFlashcard(f, currDay.value)
    ).length
  } else if (stage === specialStages.ATTEMPTED) {
    return flashcards.filter(f =>
      f.stage === stages.S1.name && isReviewedFlashcard(f, currDay.value)
    ).length
  } else if (stage == stages.S1) {
    return flashcards.filter(f =>
      f.stage === stages.S1.name && !isUnknownFlashcard(f, currDay.value) && !isReviewedFlashcard(f, currDay.value)
    ).length
  } else {
    return flashcards.filter(f => f.stage === stage.name).length
  }
}
