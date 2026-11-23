import { describe, it, expect, vi } from 'vitest'
import { QuizMetadata, ReviewSession } from '@/model/review.ts'
import { ReviewSessionType } from '@/core-logic/review-logic.ts'
import { readQuizSession } from '@/core-logic/quiz.ts'

vi.mock('@/api/api-client.ts', () => ({
  sendLatestUncompletedQuizGetRequest: vi.fn(),
}))

function quizSession(elapsedTime: number, metadata: QuizMetadata): ReviewSession {
  return {
    id: 1,
    type: ReviewSessionType.QUIZ,
    flashcardIds: [],
    elapsedTime,
    startedAt: new Date('2026-08-31T10:00:00Z'),
    metadata,
  }
}

describe('readQuizSession', () => {
  it('excludes the current round from the previous rounds time', () => {
    const session = quizSession(4000, { round: 2, elapsedTotalTime: 10_000 })

    expect(readQuizSession(session, []).elapsedPrevious).toBe(6000)
  })

  it('takes the whole total as previous for a fresh child round', () => {
    const session = quizSession(0, { round: 2, elapsedTotalTime: 10_000 })

    expect(readQuizSession(session, []).elapsedPrevious).toBe(10_000)
  })

  it('treats a missing total as no previous time', () => {
    const session = quizSession(4000, { round: 1 })

    expect(readQuizSession(session, []).elapsedPrevious).toBe(0)
  })
})
