import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { AxiosHeaders, AxiosResponse } from 'axios'
import { ref, Ref } from 'vue'
import { FlashcardSet } from '@/model/flashcard.ts'
import { Chronoday } from '@/model/chrono.ts'
import { QuizMetadata, ReviewSession } from '@/model/review.ts'
import { ReviewSessionType } from '@/core-logic/review-logic.ts'
import { chronodayStatuses } from '@/core-logic/chrono-logic.ts'
import { flashcardSetStatuses } from '@/core-logic/flashcard-logic.ts'
import { userApiErrors } from '@/api/user-api-error.ts'
import {
  createReviewSessionAttendant,
  ReviewSessionAttendant,
} from '@/core-logic/review-session-attendant.ts'
import {
  sendReviewSessionCreateRequest,
  sendReviewSessionGetRequest,
  sendReviewSessionUpdateRequest,
} from '@/api/api-client.ts'
import router from '@/router'

vi.mock('@/api/api-client.ts', () => ({
  sendReviewSessionCreateRequest: vi.fn(),
  sendReviewSessionGetRequest: vi.fn(),
  sendReviewSessionUpdateRequest: vi.fn(),
}))

vi.mock('@/router', () => ({
  default: {
    replace: vi.fn(),
    currentRoute: { value: { query: {} } },
  },
}))

const { bakeError } = vi.hoisted(() => ({ bakeError: vi.fn() }))

vi.mock('@/stores/toast-store.ts', () => ({
  useSpaceToaster: () => ({ bakeError: bakeError }),
}))

const SET_ID = 40
const CHRONODAY_ID = 42
const SESSION_ID = 1000

const createRequest = vi.mocked(sendReviewSessionCreateRequest)
const getRequest = vi.mocked(sendReviewSessionGetRequest)
const updateRequest = vi.mocked(sendReviewSessionUpdateRequest)
const routerReplace = vi.mocked(router.replace)

function okResponse<T>(data: T): AxiosResponse<T> {
  return {
    data: data,
    status: 200,
    statusText: 'OK',
    headers: {},
    config: { headers: new AxiosHeaders() },
  }
}

function flashcardSet(): FlashcardSet {
  return {
    id: SET_ID,
    name: 'Orbital Mechanics',
    status: flashcardSetStatuses.ACTIVE,
    languageId: 1,
    createdAt: new Date('2026-08-01T00:00:00Z'),
  }
}

function chronoday(): Chronoday {
  return {
    id: CHRONODAY_ID,
    chronodate: '2026-08-31',
    seqNumber: 4,
    status: chronodayStatuses.IN_PROGRESS,
    stages: [],
  }
}

function reviewSession(overrides: Partial<ReviewSession> = {}): ReviewSession {
  return {
    id: SESSION_ID,
    type: ReviewSessionType.QUIZ,
    flashcardIds: [],
    elapsedTime: 0,
    startedAt: new Date('2026-08-31T10:00:00Z'),
    ...overrides,
  }
}

/** Metadata of a quiz with one card answered and one still to go. */
function quizMetadata(overrides: Partial<QuizMetadata> = {}): QuizMetadata {
  return {
    round: 1,
    currRoundFlashcardIds: [1, 2],
    nextRoundFlashcardIds: [],
    overallCorrectCount: 1,
    overallTotalCount: 2,
    ...overrides,
  }
}

/** The update request the attendant sent on its nth flush, counting from 0. */
function flushedRequest(nth: number = 0) {
  return updateRequest.mock.calls[nth][2]
}

describe('review-session-attendant', () => {
  let attendant: ReviewSessionAttendant
  let set: Ref<FlashcardSet | undefined>
  let day: Ref<Chronoday>

  beforeEach(() => {
    vi.clearAllMocks()
    createRequest.mockResolvedValue(okResponse(reviewSession()))
    updateRequest.mockResolvedValue(okResponse(reviewSession()))
    set = ref<FlashcardSet | undefined>(flashcardSet())
    day = ref<Chronoday>(chronoday())
    attendant = createReviewSessionAttendant(ReviewSessionType.QUIZ, set, day)
  })

  afterEach(() => {
    attendant.clear()
    vi.useRealTimers()
  })

  describe('create', () => {
    it('should expose the id of the session it just created', async () => {
      await attendant.create()

      expect(attendant.sessionId.value).toBe(SESSION_ID)
    })

    it('should create the session for the current type and chronoday', async () => {
      await attendant.create({ metadata: { overallTotalCount: 12 } })

      expect(createRequest).toHaveBeenCalledWith(SET_ID, {
        type: ReviewSessionType.QUIZ,
        chronodayId: CHRONODAY_ID,
        metadata: { overallTotalCount: 12 },
      })
    })

    it('should put the new session id into the route query', async () => {
      await attendant.create()

      expect(routerReplace).toHaveBeenCalledWith({
        query: { sessionId: SESSION_ID },
      })
    })

    it('should not reach the network when the flashcard set is unknown', async () => {
      set.value = undefined

      await attendant.create()

      expect(createRequest).not.toHaveBeenCalled()
      expect(attendant.sessionId.value).toBeUndefined()
    })

    it('should hold no session and strip the route query when creation fails', async () => {
      createRequest.mockRejectedValue(new Error('boom'))

      await attendant.create({ flashcardIdsToTrack: [1, 2] })

      expect(attendant.sessionId.value).toBeUndefined()
      expect(bakeError).toHaveBeenCalledWith(
        userApiErrors.REVIEW_SESSION__CREATION_FAILED,
        undefined,
      )
      expect(routerReplace).toHaveBeenCalledWith({
        query: { sessionId: undefined },
      })

      // and: the ids handed to the failed create are gone with it
      await attendant.flush({ all: true })
      expect(updateRequest).not.toHaveBeenCalled()
    })
  })

  describe('init', () => {
    it('should adopt the session id, elapsed time and reviewed flashcards', async () => {
      attendant.init(reviewSession({ flashcardIds: [11, 22], elapsedTime: 4200 }))

      expect(attendant.sessionId.value).toBe(SESSION_ID)
      expect(attendant.elapsedTime.value).toBe(4200)

      // and: the adopted ids are replayed when the session is finished
      await attendant.flush({ all: true })
      expect(flushedRequest().flashcardIds).toEqual([{ id: 11 }, { id: 22 }])
    })

    it('should start clean for a session nobody has reviewed yet', async () => {
      // every freshly created session comes back with an empty list
      attendant.init(reviewSession({ flashcardIds: [] }))

      attendant.track(5)
      await attendant.flush({ all: true })

      expect(flushedRequest().flashcardIds).toEqual([{ id: 5 }])
    })

    it('should refuse to flush a session that is already finished', async () => {
      attendant.init(reviewSession({ finishedAt: new Date('2026-08-31T11:00:00Z') }))

      await attendant.flush({ all: true })

      expect(updateRequest).not.toHaveBeenCalled()
    })

    it('should drop the previous session', async () => {
      // a running session with a reviewed flashcard
      await attendant.create()
      attendant.track(1)

      attendant.init(reviewSession({ id: 200, flashcardIds: [9] }))

      expect(attendant.sessionId.value).toBe(200)

      // and: the flush targets the new session, carrying only its own flashcards
      updateRequest.mockResolvedValue(okResponse(reviewSession({ id: 200 })))
      await attendant.flush({ all: true })
      expect(updateRequest).toHaveBeenCalledWith(SET_ID, 200, expect.anything())
      expect(flushedRequest().flashcardIds).toEqual([{ id: 9 }])
    })
  })

  describe('loadOrCreate', () => {
    it('should resume a stored session that still has work left', async () => {
      const stored = reviewSession({
        id: 55,
        flashcardIds: [1],
        elapsedTime: 900,
        metadata: quizMetadata(),
      })
      getRequest.mockResolvedValue(okResponse(stored))
      const onboarding = vi.fn()

      await attendant.loadOrCreate({ sessionId: 55, onboarding: onboarding })

      expect(getRequest).toHaveBeenCalledWith(SET_ID, 55)
      expect(createRequest).not.toHaveBeenCalled()
      expect(onboarding).toHaveBeenCalledWith(stored)
      expect(attendant.sessionId.value).toBe(55)
      expect(attendant.elapsedTime.value).toBe(900)
    })

    it('should start a new session when the stored one cannot be fetched', async () => {
      getRequest.mockRejectedValue(new Error('gone'))

      await attendant.loadOrCreate({ sessionId: 55 })

      expect(bakeError).toHaveBeenCalledWith(
        userApiErrors.REVIEW_SESSION__FETCHING_FAILED,
        undefined,
      )
      expect(createRequest).toHaveBeenCalledOnce()
      expect(attendant.sessionId.value).toBe(SESSION_ID)
    })

    it('should start a new session when no session id is given', async () => {
      await attendant.loadOrCreate()

      expect(getRequest).not.toHaveBeenCalled()
      expect(createRequest).toHaveBeenCalledOnce()
      expect(attendant.sessionId.value).toBe(SESSION_ID)
    })
  })

  describe('onboarding a quiz', () => {
    async function loadStored(metadata: QuizMetadata | undefined) {
      getRequest.mockResolvedValue(okResponse(reviewSession({ id: 55, metadata: metadata })))
      await attendant.loadOrCreate({ sessionId: 55 })
    }

    it('should resume a round that still has cards to show', async () => {
      await loadStored(quizMetadata({ currRoundFlashcardIds: [1, 2] }))

      expect(attendant.sessionId.value).toBe(55)
    })

    it('should resume when only the next round has cards left', async () => {
      await loadStored(
        quizMetadata({
          currRoundFlashcardIds: [],
          nextRoundFlashcardIds: [3],
        }),
      )

      expect(attendant.sessionId.value).toBe(55)
    })

    it('should start over once every card has been answered correctly', async () => {
      await loadStored(quizMetadata({ overallCorrectCount: 2, overallTotalCount: 2 }))

      expect(createRequest).toHaveBeenCalledOnce()
      expect(attendant.sessionId.value).toBe(SESSION_ID)
    })

    it('should start over when neither round has cards left', async () => {
      await loadStored(
        quizMetadata({
          currRoundFlashcardIds: [],
          nextRoundFlashcardIds: [],
        }),
      )

      expect(createRequest).toHaveBeenCalledOnce()
      expect(attendant.sessionId.value).toBe(SESSION_ID)
    })

    it('should start over when the stored quiz carries no metadata', async () => {
      await loadStored(undefined)

      expect(createRequest).toHaveBeenCalledOnce()
      expect(attendant.sessionId.value).toBe(SESSION_ID)
    })
  })

  describe('onboarding a plain review', () => {
    let specialAttendant: ReviewSessionAttendant

    beforeEach(() => {
      specialAttendant = createReviewSessionAttendant(ReviewSessionType.OUTER_SPACE, set, day)
      createRequest.mockResolvedValue(
        okResponse(
          reviewSession({
            type: ReviewSessionType.OUTER_SPACE,
          }),
        ),
      )
    })

    afterEach(() => {
      specialAttendant.clear()
    })

    it('should resume a session that was never finished', async () => {
      getRequest.mockResolvedValue(
        okResponse(
          reviewSession({
            id: 55,
            type: ReviewSessionType.OUTER_SPACE,
          }),
        ),
      )

      await specialAttendant.loadOrCreate({ sessionId: 55 })

      expect(createRequest).not.toHaveBeenCalled()
      expect(specialAttendant.sessionId.value).toBe(55)
    })

    it('should start over when the stored session is already finished', async () => {
      getRequest.mockResolvedValue(
        okResponse(
          reviewSession({
            id: 55,
            type: ReviewSessionType.OUTER_SPACE,
            finishedAt: new Date('2026-08-31T11:00:00Z'),
          }),
        ),
      )

      await specialAttendant.loadOrCreate({ sessionId: 55 })

      expect(createRequest).toHaveBeenCalledOnce()
      expect(specialAttendant.sessionId.value).toBe(SESSION_ID)
    })
  })

  describe('flush', () => {
    beforeEach(async () => {
      await attendant.create()
    })

    it('should send only the last tracked flashcard while the review is running', async () => {
      attendant.track(1)
      attendant.track(2)

      await attendant.flush()

      expect(flushedRequest().flashcardIds).toEqual([{ id: 2 }])
      expect(flushedRequest().finished).toBe(false)
    })

    it('should send no flashcards when nothing has been tracked yet', async () => {
      await attendant.flush()

      expect(flushedRequest().flashcardIds).toEqual([])
    })

    it('should send every tracked flashcard when the review is finished', async () => {
      attendant.track(1)
      attendant.track(2)

      await attendant.flush({ all: true })

      expect(flushedRequest().flashcardIds).toEqual([{ id: 1 }, { id: 2 }])
      expect(flushedRequest().finished).toBe(true)
    })

    it('should finish a session only once', async () => {
      await attendant.flush({ all: true })

      // the page flushes again on the way out
      await attendant.flush({ all: true })
      await attendant.flush()

      expect(updateRequest).toHaveBeenCalledOnce()
    })

    it('should pass metadata through untouched', async () => {
      await attendant.flush({ metadata: { nextRoundFlashcardIds: [3], overallCorrectCount: 2 } })

      expect(flushedRequest().metadata).toEqual({
        nextRoundFlashcardIds: [3],
        overallCorrectCount: 2,
      })
    })

    it('should keep the session usable when an update fails', async () => {
      updateRequest.mockRejectedValueOnce(new Error('offline'))

      await attendant.flush()

      expect(bakeError).toHaveBeenCalledWith(
        userApiErrors.REVIEW_SESSION__UPDATING_FAILED,
        undefined,
      )

      // and: the session is still there to be finished
      attendant.track(1)
      await attendant.flush({ all: true })
      expect(updateRequest).toHaveBeenLastCalledWith(
        SET_ID,
        SESSION_ID,
        expect.objectContaining({
          finished: true,
          flashcardIds: [{ id: 1 }],
        }),
      )
    })

    it('should do nothing without a session', async () => {
      attendant.clear()

      await attendant.flush({ all: true })

      expect(updateRequest).not.toHaveBeenCalled()
    })
  })

  describe('touch', () => {
    beforeEach(async () => {
      await attendant.create()
    })

    it('should build a request for only the last tracked flashcard while the review is running', () => {
      attendant.track(1)
      attendant.track(2)

      expect(attendant.touch()).toMatchObject({
        flashcardIds: [{ id: 2 }],
        finished: false,
      })
    })

    it('should build a request for every tracked flashcard when the review is finished', () => {
      attendant.track(1)
      attendant.track(2)

      expect(attendant.touch({ all: true })).toMatchObject({
        flashcardIds: [{ id: 1 }, { id: 2 }],
        finished: true,
      })
    })

    it('should pass metadata through untouched', () => {
      const request = attendant.touch({
        metadata: { nextRoundFlashcardIds: [3], overallCorrectCount: 2 },
      })

      expect(request?.metadata).toEqual({
        nextRoundFlashcardIds: [3],
        overallCorrectCount: 2,
      })
    })

    it('should send nothing itself', () => {
      attendant.touch({ all: true })

      expect(updateRequest).not.toHaveBeenCalled()
    })

    it('should finish the session so that the page cannot finish it twice', async () => {
      // the carrier request finishes the session server-side
      attendant.touch({ all: true })

      // the page flushes again on the way out
      await attendant.flush({ all: true })
      await attendant.flush()

      expect(updateRequest).not.toHaveBeenCalled()
    })

    it('should build nothing more once the session is finished', () => {
      attendant.touch({ all: true })

      expect(attendant.touch()).toBeUndefined()
      expect(attendant.touch({ all: true })).toBeUndefined()
    })

    it('should build nothing for a session that was already finished when adopted', () => {
      attendant.init(reviewSession({ finishedAt: new Date('2026-08-31T11:00:00Z') }))

      expect(attendant.touch()).toBeUndefined()
    })
  })

  describe('clear', () => {
    it('should let a later session be finished again', async () => {
      // a session that has been finished
      await attendant.create()
      await attendant.flush({ all: true })

      // the page starts over
      attendant.clear()
      await attendant.create()
      await attendant.flush({ all: true })

      expect(updateRequest).toHaveBeenCalledTimes(2)
    })

    it('should forget the session, its flashcards and its elapsed time', async () => {
      attendant.init(reviewSession({ flashcardIds: [1], elapsedTime: 5000 }))

      attendant.clear()

      expect(attendant.sessionId.value).toBeUndefined()
      expect(attendant.elapsedTime.value).toBe(0)
    })
  })

  describe('elapsed time', () => {
    it('should count the time spent in the session', async () => {
      vi.useFakeTimers()
      await attendant.create()

      vi.advanceTimersByTime(2000)

      expect(attendant.elapsedTime.value).toBeGreaterThanOrEqual(2000)
      await attendant.flush()
      expect(flushedRequest().elapsedTime).toBeGreaterThanOrEqual(2000)
    })

    it('should resume counting from the elapsed time of a loaded session', async () => {
      vi.useFakeTimers()
      attendant.init(reviewSession({ elapsedTime: 30_000 }))

      vi.advanceTimersByTime(1000)

      expect(attendant.elapsedTime.value).toBeGreaterThanOrEqual(31_000)
    })

    it('should not start counting for a session that is already finished', () => {
      vi.useFakeTimers()

      attendant.init(
        reviewSession({
          elapsedTime: 5000,
          finishedAt: new Date('2026-08-31T11:00:00Z'),
        }),
      )
      vi.advanceTimersByTime(3000)

      expect(attendant.elapsedTime.value).toBe(5000)
    })

    it('should stop counting once the session is finished', async () => {
      vi.useFakeTimers()
      await attendant.create()
      vi.advanceTimersByTime(1000)
      await attendant.flush({ all: true })
      const finalTime = attendant.elapsedTime.value

      vi.advanceTimersByTime(5000)

      expect(attendant.elapsedTime.value).toBe(finalTime)
    })
  })

  describe('detached methods', () => {
    it('should keep working when its methods are pulled off the object', async () => {
      // pages are free to destructure the attendant
      const { create, track, flush, clear } = attendant

      await create()
      track(1)
      await flush({ all: true })

      expect(updateRequest).toHaveBeenCalledWith(
        SET_ID,
        SESSION_ID,
        expect.objectContaining({
          finished: true,
          flashcardIds: [{ id: 1 }],
        }),
      )

      // and:
      clear()
      expect(attendant.sessionId.value).toBeUndefined()
    })

    it('should keep working when loadOrCreate is pulled off the object', async () => {
      const { loadOrCreate } = attendant
      getRequest.mockResolvedValue(okResponse(reviewSession({ id: 55, metadata: quizMetadata() })))

      await loadOrCreate({ sessionId: 55 })

      expect(attendant.sessionId.value).toBe(55)
    })
  })
})
