import { describe, it, expect } from 'vitest'
import {
  EmptyReviewQueue,
  MonoStageReviewQueue,
  MultiStageReviewQueue,
  ReviewQueue,
} from '@/core-logic/review-logic.ts'
import { type Flashcard } from '@/model/flashcard.ts'
import { type Stage, learningStages } from '@/core-logic/stage-logic.ts'

describe('MultiStageReviewQueue', () => {
  it('should return flashcards from the highest stage first', () => {
    const f21 = flashcard(21, learningStages.S2)
    const f41 = flashcard(41, learningStages.S4)

    const flashcardMap = new Map<Stage, Flashcard[]>([
      [learningStages.S2, [f21]],
      [learningStages.S4, [f41]],
    ])

    const reviewQueue = new MultiStageReviewQueue(flashcardMap)

    expect(reviewQueue.next()).toBe(f41)
    expect(reviewQueue.next()).toBe(f21)
    expect(reviewQueue.next()).toBeUndefined()
  })

  it('should return all flashcards from a stage before moving to a lower one', () => {
    const f51 = flashcard(51, learningStages.S5)
    const f52 = flashcard(52, learningStages.S5)
    const f21 = flashcard(21, learningStages.S2)

    const flashcardMap = new Map<Stage, Flashcard[]>([
      [learningStages.S2, [f21]],
      [learningStages.S5, [f51, f52]],
    ])

    const reviewQueue = new MultiStageReviewQueue(flashcardMap)

    expect(reviewQueue.next()).toBeOneOf([f51, f52])
    expect(reviewQueue.next()).toBeOneOf([f51, f52])
    expect(reviewQueue.next()).toBe(f21)
    expect(reviewQueue.next()).toBeUndefined()
  })

  it('should return null when the queue becomes empty', () => {
    const f11 = flashcard(11, learningStages.S1)
    const flashcardMap = new Map<Stage, Flashcard[]>([[learningStages.S1, [f11]]])

    const reviewQueue = new MultiStageReviewQueue(flashcardMap)

    expect(reviewQueue.next()).toBe(f11)
    expect(reviewQueue.next()).toBeUndefined()
  })

  it('should correctly skip empty stages', () => {
    const f41 = flashcard(41, learningStages.S4)
    const f11 = flashcard(11, learningStages.S1)
    const flashcardMap = new Map<Stage, Flashcard[]>([
      [learningStages.S4, [f41]],
      [learningStages.S3, []],
      [learningStages.S1, [f11]],
    ])

    const reviewQueue = new MultiStageReviewQueue(flashcardMap)

    expect(reviewQueue.next()).toBe(f41)
    expect(reviewQueue.next()).toBe(f11)
    expect(reviewQueue.next()).toBeUndefined()
  })

  it('should return null if initialized with an empty map', () => {
    const reviewQueue = new MultiStageReviewQueue(new Map())

    expect(reviewQueue.next()).toBeUndefined()
  })

  it('should look ahead without consuming and across stages', () => {
    const f51 = flashcard(51, learningStages.S5)
    const f31 = flashcard(31, learningStages.S3)
    const f11 = flashcard(11, learningStages.S1)
    const flashcardMap = new Map<Stage, Flashcard[]>([
      [learningStages.S1, [f11]],
      [learningStages.S3, [f31]],
      [learningStages.S5, [f51]],
    ])

    const reviewQueue = new MultiStageReviewQueue(flashcardMap)

    expect(reviewQueue.lookahead(3)).toEqual([f51, f31, f11])
    expect(reviewQueue.lookahead(3)).toEqual([f51, f31, f11])
    expect(reviewQueue.remaining()).toBe(3)
    expect(queueResult(reviewQueue)).toEqual([f51, f31, f11])
  })

  it('should look ahead from the current stage after the queue has advanced', () => {
    const f52 = flashcard(52, learningStages.S5)
    const f53 = flashcard(53, learningStages.S5)
    const f21 = flashcard(21, learningStages.S2)
    const flashcardMap = new Map<Stage, Flashcard[]>([
      [learningStages.S2, [f21]],
      [learningStages.S5, [f52, f53]],
    ])

    const reviewQueue = new MultiStageReviewQueue(flashcardMap)
    reviewQueue.next()

    expect(reviewQueue.lookahead(2)).toEqual([f53, f21])
    expect(reviewQueue.next()).toBe(f53)
    expect(reviewQueue.lookahead(2)).toEqual([f21])
  })

  it('should skip empty stages when looking ahead', () => {
    const f21 = flashcard(21, learningStages.S2)
    const flashcardMap = new Map<Stage, Flashcard[]>([
      [learningStages.S5, []],
      [learningStages.S4, []],
      [learningStages.S2, [f21]],
    ])

    const reviewQueue = new MultiStageReviewQueue(flashcardMap)

    expect(reviewQueue.lookahead(1)).toEqual([f21])
    expect(reviewQueue.next()).toBe(f21)
  })

  it('should return an empty lookahead for a non-positive count', () => {
    const f11 = flashcard(11, learningStages.S1)
    const flashcardMap = new Map<Stage, Flashcard[]>([[learningStages.S1, [f11]]])

    const reviewQueue = new MultiStageReviewQueue(flashcardMap)

    expect(reviewQueue.lookahead(0)).toEqual([])
    expect(reviewQueue.lookahead(-1)).toEqual([])
  })
})

describe('MonoStageReviewQueue', () => {
  it('should return flashcards one by one in the given order', () => {
    const f11 = flashcard(1, learningStages.S1)
    const f12 = flashcard(2, learningStages.S1)
    const f13 = flashcard(3, learningStages.S1)
    const flashcards = [f11, f12, f13]

    const queue = new MonoStageReviewQueue(flashcards)

    expect(queue.next()).toBe(f11)
    expect(queue.next()).toBe(f12)
    expect(queue.next()).toBe(f13)
  })

  it('should return null after the last flashcard has been returned', () => {
    const f11 = flashcard(1, learningStages.S1)
    const flashcards = [f11]

    const queue = new MonoStageReviewQueue(flashcards)

    expect(queue.next()).toBe(f11)
    expect(queue.next()).toBeUndefined()
  })

  it('should return null if initialized with an empty array', () => {
    const queue = new MonoStageReviewQueue([])

    expect(queue.next()).toBeUndefined()
  })

  it('should not affect the original array passed to the constructor', () => {
    const f11 = flashcard(1, learningStages.S1)
    const f12 = flashcard(2, learningStages.S1)
    const originalFlashcards = [f11, f12]

    const queue = new MonoStageReviewQueue(originalFlashcards)
    queue.next()
    queue.next()

    expect(originalFlashcards.length).toBe(2)
  })

  it('shuffle() should change the order of flashcards', () => {
    const f11 = flashcard(1, learningStages.S1)
    const f21 = flashcard(1, learningStages.S2)
    const f31 = flashcard(1, learningStages.S3)
    const f41 = flashcard(1, learningStages.S4)
    const f51 = flashcard(1, learningStages.S5)
    const f61 = flashcard(1, learningStages.S1)
    const f71 = flashcard(1, learningStages.S7)
    const flashcards = [f11, f21, f31, f41, f51, f61, f71]

    const queue = new MonoStageReviewQueue(flashcards)

    const notShuffledResult = queueResult(queue)
    expect(notShuffledResult).toHaveLength(flashcards.length)
    expect(notShuffledResult).toEqual(flashcards)

    const shuffledQueue = new MonoStageReviewQueue(flashcards)
    shuffledQueue.shuffle()

    const shuffledResult = queueResult(shuffledQueue)
    expect(shuffledResult).toHaveLength(flashcards.length)
    expect(shuffledResult).not.toEqual(flashcards)
  })

  it('should return previous flashcard', () => {
    const f11 = flashcard(1, learningStages.S1)
    const f12 = flashcard(2, learningStages.S1)
    const flashcards = [f11, f12]

    const queue = new MonoStageReviewQueue(flashcards)

    expect(queue.prev()).toBeUndefined()
    expect(queue.next()).toBe(f11)
    expect(queue.prev()).toBe(f11)
    expect(queue.next()).toBe(f12)
    expect(queue.next()).toBeUndefined()
    expect(queue.prev()).toBe(f12)
    expect(queue.prev()).toBe(f11)
  })

  it('should not return null after the first flashcard has been returned', () => {
    const f41 = flashcard(1, learningStages.S4)
    const f42 = flashcard(2, learningStages.S4)
    const flashcards = [f41, f42]

    const queue = new MonoStageReviewQueue(flashcards)

    expect(queue.prev()).toBeUndefined()
    expect(queue.next()).toBe(f41)
    expect(queue.prev()).toBe(f41)
    expect(queue.prev()).toBe(f41)
    expect(queue.next()).toBe(f42)
  })

  it('should remove current flashcard and not affect the next one', () => {
    const f31 = flashcard(1, learningStages.S3)
    const f22 = flashcard(2, learningStages.S2)
    const f13 = flashcard(3, learningStages.S1)
    const flashcards = [f31, f22, f13]

    const queue = new MonoStageReviewQueue(flashcards)

    expect(queue.remaining()).toBe(3)
    expect(queue.next()).toBe(f31)
    queue.removeCurrent()
    expect(queue.remaining()).toBe(2)
    expect(queue.prev()).toBe(undefined)
    expect(queue.remaining()).toBe(2)
    expect(queue.next()).toBe(f22)
    expect(queue.next()).toBe(f13)
    expect(queue.remaining()).toBe(0)
    expect(queue.next()).toBeUndefined()
  })

  it('should remove current flashcard if there is only one in the queue', () => {
    const f11 = flashcard(1, learningStages.S1)
    const flashcards = [f11]

    const queue = new MonoStageReviewQueue(flashcards)

    expect(queue.remaining()).toBe(1)
    expect(queue.next()).toBe(f11)
    queue.removeCurrent()
    expect(queue.prev()).toBe(undefined)
    expect(queue.next()).toBe(undefined)
    expect(queue.remaining()).toBe(0)
  })

  it('should look ahead without consuming', () => {
    const f11 = flashcard(1, learningStages.S1)
    const f12 = flashcard(2, learningStages.S1)
    const f13 = flashcard(3, learningStages.S1)

    const queue = new MonoStageReviewQueue([f11, f12, f13])

    expect(queue.lookahead(2)).toEqual([f11, f12])
    expect(queue.lookahead(2)).toEqual([f11, f12])
    expect(queue.next()).toBe(f11)
    expect(queue.lookahead(2)).toEqual([f12, f13])
  })

  it('should clamp the lookahead at the end of the queue', () => {
    const f11 = flashcard(1, learningStages.S1)
    const f12 = flashcard(2, learningStages.S1)

    const queue = new MonoStageReviewQueue([f11, f12])
    queue.next()

    expect(queue.lookahead(5)).toEqual([f12])
    queue.next()
    expect(queue.lookahead(5)).toEqual([])
  })

  it('should keep the lookahead stable across removeCurrent', () => {
    const f11 = flashcard(1, learningStages.S1)
    const f12 = flashcard(2, learningStages.S1)
    const f13 = flashcard(3, learningStages.S1)

    const queue = new MonoStageReviewQueue([f11, f12, f13])
    queue.next()

    expect(queue.lookahead(2)).toEqual([f12, f13])
    queue.removeCurrent()
    expect(queue.lookahead(2)).toEqual([f12, f13])
    expect(queue.next()).toBe(f12)
  })
})

describe('EmptyReviewQueue', () => {
  it('should never look ahead', () => {
    const queue: ReviewQueue = new EmptyReviewQueue()

    expect(queue.lookahead(3)).toEqual([])
  })

  it('should never look behind', () => {
    const queue: ReviewQueue = new EmptyReviewQueue()

    expect(queue.lookbehind(3)).toEqual([])
  })
})

function flashcard(id: number, stage: Stage): Flashcard {
  return {
    id: id,
    frontSide: undefined,
    backSide: undefined,
    stage: stage.name,
    timesReviewed: 0,
    reviewHistory: { history: [] },
    creationDate: new Date().toString(),
  }
}

function queueResult(queue: ReviewQueue): Flashcard[] {
  const array: Flashcard[] = []
  let next
  while ((next = queue.next()) !== undefined) {
    array.push(next)
  }
  return array
}
