import { type Flashcard } from '@/models/flashcard.ts'
import { allLevels, stopLevels } from '@/core-logic/level-logic.ts'

// fixme implement real calendar
const calendarLevels = allLevels.filter(l => !stopLevels.includes(l)).map(l => l.name)

export function flashcardsForReview(flashcards: Flashcard[]): Flashcard[] {
  return flashcards.filter(f => calendarLevels.includes(f.level.name))
    .sort((a, b) => {
      if (a.level.order !== b.level.order) {
        return a.level.order - b.level.order
      }
      return a.id - b.id
    })
}
