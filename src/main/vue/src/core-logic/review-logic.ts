import { type Flashcard } from '@/model/flashcard.ts'
import {
  type Stage,
  getStage,
  specialStages,
  stageNameMap,
  stageOrderMap,
  learningStages,
  specialStageSet,
} from '@/core-logic/stage-logic.ts'
import type { Chronoday } from '@/model/chrono.ts'
import {
  chronodayStatusesToCompleteDay,
  selectConsecutiveDaysBefore
} from '@/core-logic/chrono-logic.ts'
import { shuffle } from '@/utils/utils.ts'

export enum ReviewMethod {
  LIGHTSPEED = 'LIGHTSPEED',
  SPECIAL = 'SPECIAL',
  SPACE = 'SPACE',
  QUIZ = 'QUIZ',
}

export interface ReviewMode {
  method: ReviewMethod
  topic: string
}

export function determineReviewMode(mode: string | undefined, stages: Stage[]): ReviewMode {
  if (mode === ReviewMethod.SPECIAL) return stagesToReviewMode(stages)

  if (mode === ReviewMethod.SPACE) return {
    method: ReviewMethod.SPACE,
    topic: specialStages.OUTER_SPACE.displayName,
  }

  if (mode === ReviewMethod.LIGHTSPEED) return {
    method: ReviewMethod.LIGHTSPEED,
    topic: '',
  }

  if (mode === ReviewMethod.QUIZ && stages.length > 0
    && stages.every(s => specialStageSet.has(s))
  ) return {
    method: ReviewMethod.QUIZ,
    topic: 'Quiz',
  }

  return stagesToReviewMode(stages)
}

function stagesToReviewMode(stages: Stage[]): ReviewMode {
  if (stages.length === 1) {
    const stage = stages[0]
    if (stage === specialStages.UNKNOWN) return {
      method: ReviewMethod.SPECIAL,
      topic: specialStages.UNKNOWN.displayName,
    }
    if (stage === specialStages.ATTEMPTED) return {
      method: ReviewMethod.SPECIAL,
      topic: specialStages.ATTEMPTED.displayName,
    }
    if (stage === specialStages.OUTER_SPACE) return {
      method: ReviewMethod.SPACE,
      topic: specialStages.OUTER_SPACE.displayName,
    }
  }

  return {
    method: ReviewMethod.LIGHTSPEED,
    topic: '',
  }
}

export interface ReviewQueue {
  shuffle(): void

  prev(): Flashcard | undefined

  next(): Flashcard | undefined

  remaining(stage?: Stage): number
}

export class EmptyReviewQueue implements ReviewQueue {
  public shuffle() {
  }

  public prev(): Flashcard | undefined {
    return undefined
  }

  public next(): Flashcard | undefined {
    return undefined
  }

  public remaining(): number {
    return 0
  }
}

export class MultiStageReviewQueue implements ReviewQueue {
  private readonly flashcardMap: Map<Stage, Flashcard[]>
  private currStage: Stage

  public constructor(flashcards: Map<Stage, Flashcard[]>) {
    this.flashcardMap = new Map()
    flashcards.forEach((flashcards, stage) => {
      this.flashcardMap.set(stage, [...flashcards])
    })
    this.currStage = learningStages.S7
  }

  public shuffle() {
    this.flashcardMap.forEach((flashcards) => {
      shuffle(flashcards)
    })
  }

  public prev(): Flashcard | undefined {
    return undefined
  }

  public next(): Flashcard | undefined {
    const flashcards = this.flashcardMap.get(this.currStage)
    if (flashcards === undefined || flashcards.length === 0) {
      const nextStage = this.nextStage()
      if (nextStage) {
        this.currStage = nextStage
        return this.next()
      }
    } else {
      return flashcards.shift()
    }
  }

  private nextStage(): Stage | undefined {
    for (let i = this.currStage.order - 1; i >= learningStages.S1.order; i--) {
      const stage = stageOrderMap.get(i)
      if (stage !== undefined && this.flashcardMap.has(stage)) {
        const flashcards = this.flashcardMap.get(stage)
        if (flashcards !== undefined && flashcards.length > 0) {
          return stage
        }
      }
    }
  }

  public remaining(stage?: Stage): number {
    if (stage) {
      return this.flashcardMap.get(stage)?.length ?? 0
    }

    let total = 0
    this.flashcardMap.forEach((flashcards) => {
      total += flashcards.length
    })
    return total
  }
}

export class MonoStageReviewQueue implements ReviewQueue {
  private readonly flashcards: Flashcard[]
  private index: number

  constructor(flashcards: Flashcard[]) {
    this.flashcards = [...flashcards]
    this.index = -1
  }

  public shuffle() {
    shuffle(this.flashcards)
  }

  public prev(): Flashcard | undefined {
    if (this.index < 0) return undefined
    if (this.index === 0) return this.flashcards[0]
    return this.flashcards[--this.index]
  }

  public next(): Flashcard | undefined {
    if (this.index >= this.flashcards.length) return undefined
    return this.flashcards[++this.index]
  }

  public remaining(): number {
    if (this.index >= this.flashcards.length) return 0
    return this.flashcards.length - (this.index + 1)
  }
}

export function createReviewQueue(
  flashcards: Flashcard[],
  currDay: Chronoday,
  chronodays: Chronoday[],
  acceptedStatuses: Set<string> = chronodayStatusesToCompleteDay,
): ReviewQueue {
  const daysForReview = selectConsecutiveDaysBefore(
    chronodays, currDay, acceptedStatuses
  )
  const stagesForReview = new Set(daysForReview.map(d => d.stages).flat())

  const result = new Map<Stage, Flashcard[]>()
  flashcards.filter(f => {
    const isStageAvailable = stagesForReview.has(f.stage)
    if (f.stage === learningStages.S1.name) {
      return isStageAvailable
        && !isUnknownFlashcard(f, currDay)
        && !isReviewedFlashcard(f, currDay)
    }
    return isStageAvailable && !isReviewedFlashcard(f, currDay)
  }).forEach(f => {
    const stage = getStage(f.stage)
    if (!result.has(stage)) {
      result.set(stage, [f])
    } else {
      result.get(stage)?.push(f)
    }
  })

  const queue = new MultiStageReviewQueue(result)
  queue.shuffle()
  return queue
}

export function createReviewQueueForStages(flashcards: Flashcard[], stages: Stage[], currDay: Chronoday): ReviewQueue {
  const stageNameSet = new Set(stages.map(s => s.name))

  const result: Flashcard[] = []
  if (stageNameSet.has(specialStages.UNKNOWN.name)) {
    result.push(...flashcards.filter(f =>
      f.stage === learningStages.S1.name && isUnknownFlashcard(f, currDay)
    ))
  } else if (stageNameSet.has(specialStages.ATTEMPTED.name)) {
    result.push(...flashcards.filter(f =>
      f.stage === learningStages.S1.name && isReviewedFlashcard(f, currDay)
    ))
  } else {
    result.push(...flashcards.filter(f => stageNameSet.has(f.stage)))
  }

  const queue = new MonoStageReviewQueue(result)
  queue.shuffle()
  return queue
}

function isUnknownFlashcard(flashcard: Flashcard, day: Chronoday): boolean {
  return flashcard.creationDate === day.chronodate
}

function isReviewedFlashcard(flashcard: Flashcard, day: Chronoday): boolean {
  return flashcard.lastReviewDate !== undefined && flashcard.lastReviewDate === day.chronodate
}

export function countFlashcards(flashcards: Flashcard[], stage: Stage, currDay: Chronoday): number {
  if (stage === specialStages.UNKNOWN) {
    return flashcards.filter(f =>
      f.stage === learningStages.S1.name && isUnknownFlashcard(f, currDay)
    ).length
  } else if (stage === specialStages.ATTEMPTED) {
    return flashcards.filter(f =>
      f.stage === learningStages.S1.name && isReviewedFlashcard(f, currDay)
    ).length
  } else if (stage == learningStages.S1) {
    return flashcards.filter(f =>
      f.stage === learningStages.S1.name && !isUnknownFlashcard(f, currDay) && !isReviewedFlashcard(f, currDay)
    ).length
  } else {
    return flashcards.filter(f => f.stage === stage.name).length
  }
}

export interface StageReview {
  stage: string
  count: number
}

export function calcStageReviews(
  flashcards: Flashcard[],
  stages: string[],
  currDay: Chronoday,
  chronodays: Chronoday[],
): StageReview[] {
  const queue = createReviewQueue(flashcards, currDay, chronodays)
  return stages
    .map(v => stageNameMap.get(v))
    .filter(v => v !== undefined)
    .sort((a, b) => b.order - a.order)
    .map(stage => {
      const count = queue.remaining(stage)
      return {
        stage: stage.displayName,
        count: count,
      }
    })
}
