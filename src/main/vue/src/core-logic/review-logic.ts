import { type Flashcard } from '@/model/flashcard.ts'
import {
  getStage,
  specialStages,
  type Stage,
  stageOrderMap,
  stages,
} from '@/core-logic/stage-logic.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import type { Chronoday } from '@/model/chrono.ts'
import { storeToRefs } from 'pinia'
import { shuffle } from '@/utils/array.ts'
import {
  isCompleteAvailable,
  selectConsecutiveDaysBeforeIncluding
} from '@/core-logic/chrono-logic.ts'

export enum ReviewMode {
  LIGHTSPEED = 'LIGHTSPEED',
  SPECIAL = 'SPECIAL',
  SPACE = 'SPACE',
}

export function toReviewMode(stage: Stage | undefined) {
  if (stage === specialStages.UNKNOWN) {
    return ReviewMode.SPECIAL
  }
  if (stage === specialStages.ATTEMPTED) {
    return ReviewMode.SPECIAL
  }
  if (stage === specialStages.OUTER_SPACE) {
    return ReviewMode.SPACE
  }
  return ReviewMode.LIGHTSPEED
}

export interface ReviewQueue {
  shuffle(): void

  prev(): Flashcard | null

  next(): Flashcard | null

  remaining(): number
}

export class EmptyReviewQueue implements ReviewQueue {
  public shuffle() {
  }

  public prev(): Flashcard | null {
    return null
  }

  public next(): Flashcard | null {
    return null
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
    this.currStage = stages.S7
  }

  public shuffle() {
    this.flashcardMap.forEach((flashcards, _) => {
      shuffle(flashcards)
    })
  }

  public prev(): Flashcard | null {
    return null
  }

  public next(): Flashcard | null {
    const flashcards = this.flashcardMap.get(this.currStage)
    if (flashcards === undefined || flashcards.length === 0) {
      const nextStage = this.nextStage()
      if (nextStage === null) {
        return null
      }
      this.currStage = nextStage
      return this.next()
    } else {
      return flashcards.shift() ?? null
    }
  }

  private nextStage(): Stage | null {
    for (let i = this.currStage.order - 1; i >= stages.S1.order; i--) {
      const stage = stageOrderMap.get(i)
      if (stage !== undefined && this.flashcardMap.has(stage)) {
        const flashcards = this.flashcardMap.get(stage)
        if (flashcards !== undefined && flashcards.length > 0) {
          return stage
        }
      }
    }
    return null
  }

  public remaining(): number {
    let total = 0
    this.flashcardMap.forEach((flashcards, _) => {
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

  public prev(): Flashcard | null {
    if (this.index < 0) return null
    if (this.index === 0) return this.flashcards[0]
    return this.flashcards[--this.index] ?? null
  }

  public next(): Flashcard | null {
    if (this.index >= this.flashcards.length) return null
    return this.flashcards[++this.index] ?? null
  }

  public remaining(): number {
    if (this.index >= this.flashcards.length) return 0
    return this.flashcards.length - (this.index + 1)
  }
}

export function createReviewQueue(flashcards: Flashcard[]): ReviewQueue {
  const chronoStore = useChronoStore()
  const currDay = chronoStore.currDay
  const daysForReview = selectConsecutiveDaysBeforeIncluding(
    chronoStore.chronodays, chronoStore.currDay, isCompleteAvailable
  )
  const stagesForReview = new Set(daysForReview.map(d => d.stages).flat())

  const result = new Map<Stage, Flashcard[]>()
  flashcards.filter(f => {
    const isStageAvailable = stagesForReview.has(f.stage)
    if (f.stage === stages.S1.name) {
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

export function createReviewQueueForStage(flashcards: Flashcard[], stage: Stage): ReviewQueue {
  const chronoStore = useChronoStore()
  const { currDay } = storeToRefs(chronoStore)

  let result: Flashcard[]
  if (stage === specialStages.UNKNOWN) {
    result = flashcards.filter(f =>
      f.stage === stages.S1.name && isUnknownFlashcard(f, currDay.value)
    )
  } else if (stage === specialStages.ATTEMPTED) {
    result = flashcards.filter(f =>
      f.stage === stages.S1.name && isReviewedFlashcard(f, currDay.value)
    )
  } else {
    result = flashcards.filter(f => f.stage === stage.name)
  }

  const queue = new MonoStageReviewQueue(result)
  queue.shuffle()
  return queue
}

function isUnknownFlashcard(flashcard: Flashcard, day: Chronoday): boolean {
  return flashcard.createdAt === day.chronodate
}

function isReviewedFlashcard(flashcard: Flashcard, day: Chronoday): boolean {
  return flashcard.reviewedAt === day.chronodate
}

export function countFlashcards(flashcards: Flashcard[], stage: Stage): number {
  const chronoStore = useChronoStore()
  const { currDay } = storeToRefs(chronoStore)

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
