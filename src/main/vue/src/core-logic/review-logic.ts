import { type Flashcard } from '@/models/flashcard.ts'
import { type Level, mainLevels, getLevel } from '@/core-logic/level-logic.ts'

// fixme implement real calendar
const calendarLevels = [...mainLevels].map(v => v.name)

export function flashcardsForReview(flashcards: Flashcard[]): Flashcard[] {
  return flashcards.filter(f => calendarLevels.includes(f.level))
    .sort((a, b) => {
      if (a.level !== b.level) {
        return getLevel(a.level).order - getLevel(b.level).order
      }
      return a.id - b.id
    })
}

export function flashcardsForLevel(flashcards: Flashcard[], level: Level): Flashcard[] {
  return flashcards.filter(f => f.level === level.name)
}
