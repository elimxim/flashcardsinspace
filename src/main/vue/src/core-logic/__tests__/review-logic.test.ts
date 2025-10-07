import { describe, it, expect } from 'vitest'
import {
  MonoStageReviewQueue,
  MultiStageReviewQueue, ReviewQueue,
} from '@/core-logic/review-logic.ts'
import { type Flashcard } from '@/model/flashcard.ts'
import { type Stage, stages } from '@/core-logic/stage-logic.ts'

describe('MultiStageReviewQueue', () => {
  it('should return flashcards from the highest stage first', () => {
    // given:
    const f21 = flashcard(21, stages.S2)
    const f41 = flashcard(41, stages.S4)

    const flashcardMap = new Map<Stage, Flashcard[]>([
      [stages.S2, [f21]],
      [stages.S4, [f41]],
    ])

    // when:
    const reviewQueue = new MultiStageReviewQueue(flashcardMap)

    // then:
    expect(reviewQueue.next()).toBe(f41)
    expect(reviewQueue.next()).toBe(f21)
    expect(reviewQueue.next()).toBeNull()
  })

  it('should return all flashcards from a stage before moving to a lower one', () => {
    // given:
    const f51 = flashcard(51, stages.S5)
    const f52 = flashcard(52, stages.S5)
    const f21 = flashcard(21, stages.S2)

    const flashcardMap = new Map<Stage, Flashcard[]>([
      [stages.S2, [f21]],
      [stages.S5, [f51, f52]],
    ])

    // when:
    const reviewQueue = new MultiStageReviewQueue(flashcardMap)

    // then:
    expect(reviewQueue.next()).toBeOneOf([f51, f52])
    expect(reviewQueue.next()).toBeOneOf([f51, f52])
    expect(reviewQueue.next()).toBe(f21)
    expect(reviewQueue.next()).toBeNull()
  })

  it('should return null when the queue becomes empty', () => {
    // given:
    const f11 = flashcard(11, stages.S1)
    const flashcardMap = new Map<Stage, Flashcard[]>([[stages.S1, [f11]]])

    // when:
    const reviewQueue = new MultiStageReviewQueue(flashcardMap)

    // then:
    expect(reviewQueue.next()).toBe(f11)
    expect(reviewQueue.next()).toBeNull()
  })

  it('should correctly skip empty stages', () => {
    // given:
    const f41 = flashcard(41, stages.S4)
    const f11 = flashcard(11, stages.S1)
    const flashcardMap = new Map<Stage, Flashcard[]>([
      [stages.S4, [f41]],
      [stages.S3, []],
      [stages.S1, [f11]],
    ])

    // when:
    const reviewQueue = new MultiStageReviewQueue(flashcardMap)

    // then:
    expect(reviewQueue.next()).toBe(f41)
    expect(reviewQueue.next()).toBe(f11)
    expect(reviewQueue.next()).toBeNull()
  })

  it('should return null if initialized with an empty map', () => {
    // given:
    // when:
    const reviewQueue = new MultiStageReviewQueue(new Map())

    // then:
    expect(reviewQueue.next()).toBeNull()
  })
})

describe('MonoStageReviewQueue', () => {
  it('should return flashcards one by one in the given order', () => {
    // given:
    const f11 = flashcard(1, stages.S1)
    const f12 = flashcard(2, stages.S1)
    const f13 = flashcard(3, stages.S1)
    const flashcards = [f11, f12, f13]

    // when:
    const queue = new MonoStageReviewQueue(flashcards)

    // then:
    expect(queue.next()).toBe(f11)
    expect(queue.next()).toBe(f12)
    expect(queue.next()).toBe(f13)
  })

  it('should return null after the last flashcard has been returned', () => {
    // given:
    const f11 = flashcard(1, stages.S1)
    const flashcards = [f11]

    // when:
    const queue = new MonoStageReviewQueue(flashcards)

    // then:
    expect(queue.next()).toBe(f11)
    expect(queue.next()).toBeNull()
  })

  it('should return null if initialized with an empty array', () => {
    // given:
    // when:
    const queue = new MonoStageReviewQueue([])

    // then:
    expect(queue.next()).toBeNull()
  })

  it('should not affect the original array passed to the constructor', () => {
    // given:
    const f11 = flashcard(1, stages.S1)
    const f12 = flashcard(2, stages.S1)
    const originalFlashcards = [f11, f12]

    // when:
    const queue = new MonoStageReviewQueue(originalFlashcards)
    queue.next()
    queue.next()

    // then:
    expect(originalFlashcards.length).toBe(2)
  })

  it('shuffle() should change the order of flashcards', () => {
    // given:
    const f11 = flashcard(1, stages.S1)
    const f21 = flashcard(1, stages.S2)
    const f31 = flashcard(1, stages.S3)
    const f41 = flashcard(1, stages.S4)
    const f51 = flashcard(1, stages.S5)
    const f61 = flashcard(1, stages.S1)
    const f71 = flashcard(1, stages.S7)
    const flashcards = [f11, f21, f31, f41, f51, f61, f71]

    // when:
    const queue = new MonoStageReviewQueue(flashcards)

    // then
    const notShuffledResult = queueResult(queue)
    expect(notShuffledResult).toHaveLength(flashcards.length)
    expect(notShuffledResult).toEqual(flashcards)

    // when:
    const shuffledQueue = new MonoStageReviewQueue(flashcards)
    shuffledQueue.shuffle()

    // then:
    const shuffledResult = queueResult(shuffledQueue)
    expect(shuffledResult).toHaveLength(flashcards.length)
    expect(shuffledResult).not.toEqual(flashcards)
  })

  it('should return previous flashcard', () => {
    // given:
    const f11 = flashcard(1, stages.S1)
    const f12 = flashcard(2, stages.S1)
    const flashcards = [f11, f12]

    // when:
    const queue = new MonoStageReviewQueue(flashcards)

    // then:
    expect(queue.prev()).toBeNull()
    expect(queue.next()).toBe(f11)
    expect(queue.prev()).toBe(f11)
    expect(queue.next()).toBe(f12)
    expect(queue.next()).toBeNull()
    expect(queue.prev()).toBe(f12)
    expect(queue.prev()).toBe(f11)
  })

  it('should not return null after the first flashcard has been returned', () => {
    // given:
    const f41 = flashcard(1, stages.S4)
    const f42 = flashcard(2, stages.S4)
    const flashcards = [f41, f42]

    // when:
    const queue = new MonoStageReviewQueue(flashcards)

    // then:
    expect(queue.prev()).toBeNull()
    expect(queue.next()).toBe(f41)
    expect(queue.prev()).toBe(f41)
    expect(queue.prev()).toBe(f41)
    expect(queue.next()).toBe(f42)
  })

})

function flashcard(id: number, stage: Stage): Flashcard {
  return {
    id: id,
    frontSide: '',
    backSide: '',
    stage: stage.name,
    reviewCount: 0,
    reviewHistory: { history: [] },
    createdAt: new Date().toString(),
    reviewedAt: null,
    lastUpdatedAt: null,
  }
}

function queueResult(queue: ReviewQueue): Flashcard[] {
  const array: Flashcard[] = []
  let next
  while ((next = queue.next()) !== null) {
    array.push(next)
  }
  return array
}
