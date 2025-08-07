import { type Flashcard } from '@/model/flashcard.ts'
import { type Stage, getStage, stages, specialStages } from '@/core-logic/stage-logic.ts'
import { useTimelineStore } from '@/stores/timeline-store.ts'
import type { Chronoday } from '@/model/timeline.ts'
import { storeToRefs } from 'pinia'

export function findFlashcardsForReview(flashcards: Flashcard[]): Flashcard[] {
  const timelineStore = useTimelineStore()
  const currDay = timelineStore.currDay
  const currDayStages = timelineStore.currLightDayStages

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

function isUnknownFlashcard(flashcard: Flashcard, lightDay: Chronoday): boolean {
  return flashcard.createdAt === lightDay.chronodate
}

function isReviewedFlashcard(flashcard: Flashcard, lightDay: Chronoday): boolean {
  return flashcard.reviewedAt === lightDay.chronodate
}

export function flashcardsForStage(flashcards: Flashcard[], stage: Stage): Flashcard[] {
  const timelineStore = useTimelineStore()
  const { currDay } = storeToRefs(timelineStore)

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
  const timelineStore = useTimelineStore()
  const { currDay } = storeToRefs(timelineStore)

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
