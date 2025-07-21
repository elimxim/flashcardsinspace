export interface Level {
  name: string
  order: number
}

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

export const allLevels: Level[] = [
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

export const mainLevels = new Set([
  levels.FIRST,
  levels.SECOND,
  levels.THIRD,
  levels.FORTH,
  levels.FIFTH,
  levels.SIXTH,
  levels.SEVENTH,
])

export const specialLevels = new Set([
  levels.UNKNOWN,
  levels.ATTEMPTED,
  levels.OUTER_SPACE,
])

/**
 * Determines and returns the next level based on the current level
 * with the following exclusions:
 * - Flashcards cannot move above the "OUTER_SPACE" level(last level);
 * - Flashcards cannot move from "UNKNOWN" or "ATTEMPTED" levels
 * (it happens when the calendar day is switched).
 */
export function nextLevel(levelName: string): Level {
  const level = getLevel(levelName)
  if (specialLevels.has(level)) {
    console.log("What the heck " + level)
    return level
  }
  console.log("What the heck??? " + level)

  const nextLevel = levelOrderMap.get(level.order + 1)
  if (nextLevel === undefined) {
    throw new Error(`Couldn't get next level for ${level}`)
  }
  return nextLevel
}

/**
 * Determines and returns the previous level of a flashcard according
 * to the following criteria:
 * - If the flashcard's in the “FIRST” level or above, it moves to the “ATTEMPTED” level;
 * - Flashcards in "UNKNOWN", "ATTEMPTED", and "OUTER_SPACE" levels cannot move down.
 */
export function prevLevel(levelName: string): Level {
  let level = getLevel(levelName)
  if (specialLevels.has(level)) {
    return level
  }

  return levels.ATTEMPTED
}

export function getLevel(name: string): Level {
  const level = levelNameMap.get(name)
  if (level === undefined) {
    throw new Error(`Couldn't find level by name=${name}`)
  }
  return level
}
