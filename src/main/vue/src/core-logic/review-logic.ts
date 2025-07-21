import { type Flashcard } from '@/models/flashcard.ts'
import { type Level, mainLevels, levelNameMap } from '@/core-logic/level-logic.ts'

// fixme implement real calendar
const calendarLevels = [...mainLevels].map(v => v.name)

export function flashcardsForReview(flashcards: Flashcard[]): Flashcard[] {
  return flashcards.filter(f => calendarLevels.includes(f.level))
    .sort((a, b) => {
      if (a.level !== b.level) {
        const l1 = levelNameMap.get(a.level)?.order ?? 0
        const l2 = levelNameMap.get(b.level)?.order ?? 0
        return l1 - l2
      }
      return a.id - b.id
    })
}

export function flashcardsForLevel(flashcards: Flashcard[], level: Level): Flashcard[] {
  return flashcards.filter(f => f.level === level.name)
}
