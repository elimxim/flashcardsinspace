import { type Level } from '@/models/flashcard.ts'

export const levels = {
  UNKNOWN: { name: 'Unknown', order: 0 } as Level,
  ATTEMPTED: { name: 'Attempted', order: 0 } as Level,
  FIRST: { name: 'Level 1', order: 1 } as Level,
  SECOND: { name: 'Level 2', order: 2 } as Level,
  THIRD: { name: 'Level 3', order: 3 } as Level,
  FORTH: { name: 'Level 4', order: 4 } as Level,
  FIFTH: { name: 'Level 5', order: 5 } as Level,
  SIXTH: { name: 'Level 6', order: 6 } as Level,
  SEVENTH: { name: 'Level 7', order: 7 } as Level,
  OUTER_SPACE: { name: 'Outer space', order: 8 } as Level,
}

export const levelNames: string[] = [
  levels.UNKNOWN.name,
  levels.ATTEMPTED.name,
  levels.FIRST.name,
  levels.SECOND.name,
  levels.THIRD.name,
  levels.FORTH.name,
  levels.FIFTH.name,
  levels.SIXTH.name,
  levels.SEVENTH.name,
  levels.OUTER_SPACE.name,
]

export const levelOrderMap = new Map<number, Level>([
  [levels.UNKNOWN.order, levels.UNKNOWN],
  [levels.ATTEMPTED.order, levels.ATTEMPTED],
  [levels.FIRST.order, levels.FIRST],
  [levels.SECOND.order, levels.SECOND],
  [levels.THIRD.order, levels.THIRD],
  [levels.FORTH.order, levels.FORTH],
  [levels.FIFTH.order, levels.FIFTH],
  [levels.SIXTH.order, levels.SIXTH],
  [levels.SEVENTH.order, levels.SEVENTH],
  [levels.OUTER_SPACE.order, levels.OUTER_SPACE],
])

export const levelNameMap = new Map<string, Level>([
  [levels.UNKNOWN.name, levels.UNKNOWN],
  [levels.ATTEMPTED.name, levels.ATTEMPTED],
  [levels.FIRST.name, levels.FIRST],
  [levels.SECOND.name, levels.SECOND],
  [levels.THIRD.name, levels.THIRD],
  [levels.FORTH.name, levels.FORTH],
  [levels.FIFTH.name, levels.FIFTH],
  [levels.SIXTH.name, levels.SIXTH],
  [levels.SEVENTH.name, levels.SEVENTH],
  [levels.OUTER_SPACE.name, levels.OUTER_SPACE],
])

export const allLevels = [
  levels.UNKNOWN,
  levels.ATTEMPTED,
  levels.FIRST,
  levels.SECOND,
  levels.THIRD,
  levels.FORTH,
  levels.FIFTH,
  levels.SIXTH,
  levels.SEVENTH,
  levels.OUTER_SPACE,
]

export const stopLevels = [
  levels.UNKNOWN,
  levels.ATTEMPTED,
  levels.OUTER_SPACE,
]

/**
 * Determines and returns the next level based on the current level.
 *
 * With the following exclusions:
 * - Flashcards cannot move above the "OUTER_SPACE" level(last level);
 * - Flashcards cannot move from "UNKNOWN" or "ATTEMPTED" levels
 * (it happens when the calendar day is switched).
 */
export function nextLevel(level: Level): Level {
  if (stopLevels.includes(level)) {
    return level
  }

  const nextLevel = levelOrderMap.get(level.order + 1)
  if (nextLevel === undefined) {
    throw new Error(`Can't get next level for ${level}`)
  }
  return nextLevel
}
