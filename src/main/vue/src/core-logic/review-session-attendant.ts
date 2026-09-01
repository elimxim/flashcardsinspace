import { ReviewSessionCreateRequest, ReviewSessionUpdateRequest } from '@/api/communication.ts'
import {
  sendReviewSessionCreateRequest,
  sendReviewSessionGetRequest,
  sendReviewSessionUpdateRequest,
} from '@/api/api-client.ts'
import { Log, LogTag } from '@/utils/logger.ts'
import { userApiErrors } from '@/api/user-api-error.ts'
import { FlashcardSet } from '@/model/flashcard.ts'
import router from '@/router'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { ref, Ref } from 'vue'
import { ReviewSessionType } from '@/core-logic/review-logic.ts'
import { Chronoday } from '@/model/chrono.ts'
import { errorResponseData } from '@/core-logic/media-error.ts'
import { useStopWatch } from '@/utils/stop-watch.ts'
import { ReviewSession } from '@/model/review.ts'

export interface CreateOptions {
  elapsedTime?: number
  flashcardIdsToTrack?: number[]
  metadata?: Record<string, unknown> | undefined
}

export interface LoadOrCreateOptions extends CreateOptions {
  sessionId?: number | undefined
  onboarding?: (session: ReviewSession) => void
}

export interface FlushOptions {
  metadata?: Record<string, unknown> | undefined
  all?: boolean
}

export interface ReviewSessionAttendant {
  readonly sessionId: Ref<number | undefined>
  readonly elapsedTime: Ref<number>

  init(session: ReviewSession): void

  create(options?: CreateOptions): Promise<void>

  loadOrCreate(options?: LoadOrCreateOptions): Promise<void>

  flush(options?: FlushOptions): Promise<void>

  touch(options?: FlushOptions): ReviewSessionUpdateRequest | undefined

  track(flashcardId: number): void

  clear(): void
}

export function createReviewSessionAttendant(
  sessionType: ReviewSessionType,
  flashcardSet: Ref<FlashcardSet | undefined>,
  chronoDay: Ref<Chronoday>,
): ReviewSessionAttendant {
  const sessionId: Ref<number | undefined> = ref()
  const sessionElapsedTime = ref(0)
  const { startWatch, stopWatch, resetWatch } = useStopWatch(sessionElapsedTime)

  const trackedFlashcardIds: number[] = []
  let finished = false

  function init(session: ReviewSession) {
    clear()

    sessionId.value = session.id
    trackedFlashcardIds.push(...session.flashcardIds)
    sessionElapsedTime.value = session.elapsedTime
    finished = !!session.finishedAt

    if (!finished) startWatch()
  }

  async function create(options: CreateOptions = {}) {
    const { elapsedTime = 0, flashcardIdsToTrack = [], metadata = undefined } = options

    if (!flashcardSet.value) {
      Log.log(
        LogTag.SYSTEM,
        `Can't create review session ${sessionType}, FlashcardSet is undefined`,
      )
      return Promise.resolve()
    }

    clear()

    sessionElapsedTime.value = elapsedTime
    trackedFlashcardIds.push(...flashcardIdsToTrack)

    const request: ReviewSessionCreateRequest = {
      type: sessionType,
      chronodayId: chronoDay.value.id,
      metadata: metadata,
    }

    const reviewSession = await createReviewSession(flashcardSet.value, request)
    if (!reviewSession) {
      clear()
      await router.replace({
        query: {
          ...router.currentRoute.value.query,
          sessionId: undefined,
        },
      })
      return
    } else {
      sessionId.value = reviewSession.id
      await router.replace({
        query: {
          ...router.currentRoute.value.query,
          sessionId: reviewSession.id,
        },
      })

      startWatch()
    }
  }

  async function loadOrCreate(options: LoadOrCreateOptions = {}) {
    const {
      elapsedTime = 0,
      flashcardIdsToTrack = [],
      metadata = undefined,
      sessionId = undefined,
      onboarding = () => {},
    } = options

    if (!flashcardSet.value) {
      Log.log(
        LogTag.SYSTEM,
        `Can't create review session ${sessionType}, FlashcardSet is undefined`,
      )
      return Promise.resolve()
    }

    clear()

    if (sessionId) {
      const existingSession = await fetchReviewSession(flashcardSet.value, sessionId)
      if (existingSession && canBeOnboarded(existingSession)) {
        onboarding(existingSession)
        init(existingSession)
      } else {
        await create({ elapsedTime, flashcardIdsToTrack, metadata })
      }
    } else {
      await create({ elapsedTime, flashcardIdsToTrack, metadata })
    }
  }

  function canBeOnboarded(session: ReviewSession): boolean {
    if (session.type === ReviewSessionType.QUIZ) {
      const total = session.metadata?.overallTotalCount ?? 0
      const correct = session.metadata?.overallCorrectCount ?? 0
      const currRoundLength = session.metadata?.currRoundFlashcardIds?.length ?? 0
      const nextRoundLength = session.metadata?.nextRoundFlashcardIds?.length ?? 0
      return total !== correct && (currRoundLength !== 0 || nextRoundLength !== 0)
    }
    return !session.finishedAt
  }

  async function flush(options: FlushOptions = {}): Promise<void> {
    if (finished) return

    if (!flashcardSet.value) {
      Log.log(LogTag.SYSTEM, `Can't flush review session ${sessionType}, FlashcardSet is undefined`)
      return Promise.resolve()
    } else if (!sessionId.value) {
      Log.log(
        LogTag.SYSTEM,
        `Can't flush lost review session for FlashcardSet ${flashcardSet.value.id}`,
      )
      return Promise.resolve()
    }

    const request = touch(options)
    if (!request) return

    await updateReviewSession(flashcardSet.value, sessionId.value, request)
  }

  function touch(options: FlushOptions = {}) {
    if (finished) return

    const { metadata = undefined, all = false } = options

    const request: ReviewSessionUpdateRequest = {
      elapsedTime: sessionElapsedTime.value,
      finished: all,
      metadata: metadata,
    }

    if (all) {
      finished = true
      stopWatch()

      request.flashcardIds = [...trackedFlashcardIds].map((id) => ({ id: id }))
    } else {
      const lastFlashcardId = trackedFlashcardIds.at(-1)
      request.flashcardIds = lastFlashcardId ? [{ id: lastFlashcardId }] : []
    }

    return request
  }

  function track(flashcardId: number) {
    trackedFlashcardIds.push(flashcardId)
  }

  function clear() {
    sessionId.value = undefined
    finished = false
    trackedFlashcardIds.length = 0
    resetWatch()
  }

  return {
    sessionId: sessionId,
    elapsedTime: sessionElapsedTime,
    init,
    create,
    loadOrCreate,
    flush,
    touch,
    track,
    clear,
  }
}

export async function fetchReviewSession(
  flashcardSet: FlashcardSet,
  sessionId: number,
): Promise<ReviewSession | undefined> {
  return await sendReviewSessionGetRequest(flashcardSet.id, sessionId)
    .then((response) => {
      return response.data
    })
    .catch((error) => {
      Log.error(LogTag.LOGIC, `Failed to fetch review session ${sessionId}`, error.response?.data)
      useSpaceToaster().bakeError(
        userApiErrors.REVIEW_SESSION__FETCHING_FAILED,
        error.response?.data,
      )
      return undefined
    })
}

export async function createReviewSession(
  flashcardSet: FlashcardSet,
  request: ReviewSessionCreateRequest,
): Promise<ReviewSession | undefined> {
  try {
    const response = await sendReviewSessionCreateRequest(flashcardSet.id, request)
    Log.log(LogTag.LOGIC, `Review session ${request.type}-${response.data.id} created`)
    return response.data
  } catch (error) {
    Log.error(LogTag.LOGIC, `Failed to create review session ${request.type}`, error)
    useSpaceToaster().bakeError(
      userApiErrors.REVIEW_SESSION__CREATION_FAILED,
      errorResponseData(error),
    )
    return undefined
  }
}

export async function updateReviewSession(
  flashcardSet: FlashcardSet,
  sessionId: number,
  request: ReviewSessionUpdateRequest,
): Promise<ReviewSession | undefined> {
  try {
    const response = await sendReviewSessionUpdateRequest(flashcardSet.id, sessionId, request)
    Log.log(LogTag.LOGIC, `Review session ${sessionId} updated`)
    return response.data
  } catch (error) {
    Log.error(LogTag.LOGIC, `Failed to update review session ${sessionId}`, error)
    useSpaceToaster().bakeError(
      userApiErrors.REVIEW_SESSION__UPDATING_FAILED,
      errorResponseData(error),
    )
    return undefined
  }
}
