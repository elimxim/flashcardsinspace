export interface Stage {
  name: string
  displayName: string
  order: number
}

export const specialStages = {
  UNKNOWN: { name: 'UNKNOWN', displayName: 'Unknown', order: 0 } as Stage,
  ATTEMPTED: { name: 'ATTEMPTED', displayName: 'Attempted', order: 0 } as Stage,
  OUTER_SPACE: { name: 'OUTER_SPACE', displayName: 'Outer space', order: 8 } as Stage,
}

export const learningStages = {
  S1: { name: 'S1', displayName: 'Stage 1', order: 1 } as Stage,
  S2: { name: 'S2', displayName: 'Stage 2', order: 2 } as Stage,
  S3: { name: 'S3', displayName: 'Stage 3', order: 3 } as Stage,
  S4: { name: 'S4', displayName: 'Stage 4', order: 4 } as Stage,
  S5: { name: 'S5', displayName: 'Stage 5', order: 5 } as Stage,
  S6: { name: 'S6', displayName: 'Stage 6', order: 6 } as Stage,
  S7: { name: 'S7', displayName: 'Stage 7', order: 7 } as Stage,
}

export const allStages: Stage[] = [
  specialStages.UNKNOWN,
  specialStages.ATTEMPTED,
  learningStages.S1,
  learningStages.S2,
  learningStages.S3,
  learningStages.S4,
  learningStages.S5,
  learningStages.S6,
  learningStages.S7,
  specialStages.OUTER_SPACE,
]

export const stageOrderMap = new Map<number, Stage>([
  [learningStages.S1.order, learningStages.S1],
  [learningStages.S2.order, learningStages.S2],
  [learningStages.S3.order, learningStages.S3],
  [learningStages.S4.order, learningStages.S4],
  [learningStages.S5.order, learningStages.S5],
  [learningStages.S6.order, learningStages.S6],
  [learningStages.S7.order, learningStages.S7],
  [specialStages.OUTER_SPACE.order, specialStages.OUTER_SPACE],
])

export const stageNameMap = new Map<string, Stage>([
  [specialStages.UNKNOWN.name, specialStages.UNKNOWN],
  [specialStages.ATTEMPTED.name, specialStages.ATTEMPTED],
  [learningStages.S1.name, learningStages.S1],
  [learningStages.S2.name, learningStages.S2],
  [learningStages.S3.name, learningStages.S3],
  [learningStages.S4.name, learningStages.S4],
  [learningStages.S5.name, learningStages.S5],
  [learningStages.S6.name, learningStages.S6],
  [learningStages.S7.name, learningStages.S7],
  [specialStages.OUTER_SPACE.name, specialStages.OUTER_SPACE],
])

export const mainStageArray = [
  learningStages.S1,
  learningStages.S2,
  learningStages.S3,
  learningStages.S4,
  learningStages.S5,
  learningStages.S6,
  learningStages.S7,
]

export const specialStageSet = new Set([
  specialStages.UNKNOWN,
  specialStages.ATTEMPTED,
  specialStages.OUTER_SPACE,
])

export function toSortedOrderNumbers(stages: string[]): number[] {
  return stages.map(name =>
    stageNameMap.get(name.toString())?.order
  ).filter(order =>
    order !== undefined
  ).sort(
    (a, b) => b - a
  )
}

/**
 * Determines and returns the next stage based on the current stage
 * with the following exclusion:
 * - flashcards cannot move from
 * "UNKNOWN", "ATTEMPTED" or "OUTER_SPACE" stages
 */
export function nextStage(name: string): Stage {
  const stage = getStage(name)
  if (specialStageSet.has(stage)) {
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
  const stage = getStage(name)
  if (specialStageSet.has(stage)) {
    return stage
  }

  return learningStages.S1
}

export function getStage(name: string): Stage {
  const stage = stageNameMap.get(name)
  if (stage === undefined) {
    throw new Error(`Couldn't find stage by name=${name}`)
  }
  return stage
}

export function toStage(value: unknown): Stage | undefined {
  if (typeof value === 'string') {
    const str = String(value).trim().toUpperCase()
    return stageNameMap.get(str)
  }
}
