export interface Stage {
  name: string
  order: number
}

export const stages = {
  // special stage (part of FIRST stage) for recently added flashcards
  UNKNOWN: { name: 'Unknown', order: 0 } as Stage,
  // special stage (part of FIRST stage) for attempted flashcards
  ATTEMPTED: { name: 'Attempted', order: 0 } as Stage,
  FIRST: { name: 'Stage 1', order: 1 } as Stage,
  SECOND: { name: 'Stage 2', order: 2 } as Stage,
  THIRD: { name: 'Stage 3', order: 3 } as Stage,
  FORTH: { name: 'Stage 4', order: 4 } as Stage,
  FIFTH: { name: 'Stage 5', order: 5 } as Stage,
  SIXTH: { name: 'Stage 6', order: 6 } as Stage,
  SEVENTH: { name: 'Stage 7', order: 7 } as Stage,
  // special stage for memorized flashcards
  OUTER_SPACE: { name: 'Outer space', order: 8 } as Stage,
}

export const allStages: Stage[] = [
  stages.UNKNOWN,
  stages.ATTEMPTED,
  stages.FIRST,
  stages.SECOND,
  stages.THIRD,
  stages.FORTH,
  stages.FIFTH,
  stages.SIXTH,
  stages.SEVENTH,
  stages.OUTER_SPACE,
]

export const stageOrderMap = new Map<number, Stage>([
  [stages.UNKNOWN.order, stages.UNKNOWN],
  [stages.ATTEMPTED.order, stages.ATTEMPTED],
  [stages.FIRST.order, stages.FIRST],
  [stages.SECOND.order, stages.SECOND],
  [stages.THIRD.order, stages.THIRD],
  [stages.FORTH.order, stages.FORTH],
  [stages.FIFTH.order, stages.FIFTH],
  [stages.SIXTH.order, stages.SIXTH],
  [stages.SEVENTH.order, stages.SEVENTH],
  [stages.OUTER_SPACE.order, stages.OUTER_SPACE],
])

export const stageNameMap = new Map<string, Stage>([
  [stages.UNKNOWN.name, stages.UNKNOWN],
  [stages.ATTEMPTED.name, stages.ATTEMPTED],
  [stages.FIRST.name, stages.FIRST],
  [stages.SECOND.name, stages.SECOND],
  [stages.THIRD.name, stages.THIRD],
  [stages.FORTH.name, stages.FORTH],
  [stages.FIFTH.name, stages.FIFTH],
  [stages.SIXTH.name, stages.SIXTH],
  [stages.SEVENTH.name, stages.SEVENTH],
  [stages.OUTER_SPACE.name, stages.OUTER_SPACE],
])

export const mainStages = new Set([
  stages.FIRST,
  stages.SECOND,
  stages.THIRD,
  stages.FORTH,
  stages.FIFTH,
  stages.SIXTH,
  stages.SEVENTH,
])

export const specialStages = new Set([
  stages.UNKNOWN,
  stages.ATTEMPTED,
  stages.OUTER_SPACE,
])

/**
 * Determines and returns the next stage based on the current stage
 * with the following exclusions:
 * - Flashcards cannot move above the "OUTER_SPACE" stage (last stages);
 * - Flashcards cannot move from "UNKNOWN" or "ATTEMPTED" stages
 * (it happens when the calendar day is switched).
 */
export function nextStage(name: string): Stage {
  const stage = getStage(name)
  if (specialStages.has(stage)) {
    return stage
  }

  const nextStage = stageOrderMap.get(stage.order + 1)
  if (nextStage === undefined) {
    throw new Error(`Couldn't get next stage for ${stage}`)
  }
  return nextStage
}

/**
 * Determines and returns the previous stage of a flashcard according
 * to the following criteria:
 * - If the flashcard's in the “FIRST” stage or above, it moves to the “ATTEMPTED” stage;
 * - Flashcards in "UNKNOWN", "ATTEMPTED", and "OUTER_SPACE" stages cannot move down.
 */
export function prevStage(name: string): Stage {
  let stage = getStage(name)
  if (specialStages.has(stage)) {
    return stage
  }

  return stages.ATTEMPTED
}

export function getStage(name: string): Stage {
  const stage = stageNameMap.get(name)
  if (stage === undefined) {
    throw new Error(`Couldn't find stage by name=${name}`)
  }
  return stage
}
