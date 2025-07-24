import { type Flashcard } from '@/models/flashcard.ts'
import { type Stage, mainStages, getStage } from '@/core-logic/stage-logic.ts'

// fixme implement real calendar
const calendarStages = [...mainStages].map(v => v.name)

export function flashcardsForReview(flashcards: Flashcard[]): Flashcard[] {
  return flashcards.filter(f => calendarStages.includes(f.stage))
    .sort((a, b) => {
      if (a.stage !== b.stage) {
        return getStage(a.stage).order - getStage(b.stage).order
      }
      return a.id - b.id
    })
}

export function flashcardsForStage(flashcards: Flashcard[], stage: Stage): Flashcard[] {
  return flashcards.filter(f => f.stage === stage.name)
}
