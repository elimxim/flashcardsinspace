import {
  ReviewSessionCreateRequest,
  ReviewSessionUpdateRequest,
} from "@/api/communication.ts"
import {
  sendReviewSessionCreateRequest,
  sendReviewSessionGetRequest,
  sendReviewSessionUpdateRequest,
} from "@/api/api-client.ts"
import { Log, LogTag } from "@/utils/logger.ts"
import { userApiErrors } from "@/api/user-api-error.ts"
import { FlashcardSet } from "@/model/flashcard.ts"
import router from "@/router"
import { useSpaceToaster } from "@/stores/toast-store.ts"
import { ref, Ref } from "vue"
import { ReviewSessionType } from "@/core-logic/review-logic.ts"
import { Chronoday } from "@/model/chrono.ts"
import { errorResponseData } from "@/core-logic/media-error.ts"
import { useStopWatch } from "@/utils/stop-watch.ts"
import { ReviewSession } from "@/model/review.ts"

export interface CreateOptions {
  elapsedTime?: number
  idsToTrack?: number[],
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
  readonly sessionId: number | undefined
  readonly elapsedTime: Ref<number>

  init(session: ReviewSession): void

  create(options?: CreateOptions): Promise<void>

  loadOrCreate(options?: LoadOrCreateOptions): Promise<void>

  flush(options?: FlushOptions): Promise<void>

  clear(): void

  track(flashcardId: number): void
}

export function createReviewSessionAttendant(
  sessionType: ReviewSessionType,
  flashcardSet: Ref<FlashcardSet | undefined>,
  chronoDay: Ref<Chronoday>,
): ReviewSessionAttendant {
  let reviewSession: ReviewSession | undefined
  let finished = false
  const sessionElapsedTime = ref(0)

  const {
    startWatch,
    stopWatch,
    resetWatch,
  } = useStopWatch(sessionElapsedTime)

  const trackedFlashcardIds: number[] = []

  async function flush(options: FlushOptions = {}): Promise<void> {
    if (finished) return
    const { metadata = undefined, all = false } = options

    if (!flashcardSet.value) {
      Log.log(LogTag.SYSTEM, `Can't flush review session ${sessionType}, FlashcardSet is undefined`)
      return Promise.resolve()
    } else if (!reviewSession) {
      Log.log(LogTag.SYSTEM, `Can't flush lost review session for FlashcardSet ${flashcardSet.value.id}`)
      return Promise.resolve()
    }

    const request: ReviewSessionUpdateRequest = {
      elapsedTime: sessionElapsedTime.value,
      finished: all,
      metadata: metadata,
    }

    if (all) {
      finished = true
      stopWatch()
      request.flashcardIds = [...trackedFlashcardIds].map(id => ({ id: id }))
    } else {
      const lastFlashcardId = trackedFlashcardIds.at(-1)
      request.flashcardIds = lastFlashcardId ? [{ id: lastFlashcardId }] : []
    }

    const result = await updateReviewSession(flashcardSet.value, reviewSession.id, request)
    if (result) reviewSession = result
  }

  function clear() {
    reviewSession = undefined
    finished = false
    trackedFlashcardIds.length = 0
    resetWatch()
  }

  function track(flashcardId: number) {
    trackedFlashcardIds.push(flashcardId)
  }

  function init(session: ReviewSession) {
    clear()

    reviewSession = session
    trackedFlashcardIds.push(...session.flashcardIds)
    sessionElapsedTime.value = session.elapsedTime
    finished = !!session.finishedAt

    startWatch()
  }

  async function create(options: CreateOptions = {}) {
    const { elapsedTime = 0, idsToTrack = [], metadata = undefined } = options

    if (!flashcardSet.value) {
      Log.log(LogTag.SYSTEM, `Can't create review session ${sessionType}, FlashcardSet is undefined`)
      return Promise.resolve()
    }

    clear()

    sessionElapsedTime.value = elapsedTime
    trackedFlashcardIds.push(...idsToTrack)

    const request: ReviewSessionCreateRequest = {
      type: sessionType,
      chronodayId: chronoDay.value.id,
      metadata: metadata,
    }

    reviewSession = await createReviewSession(flashcardSet.value, request)
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
      idsToTrack = [],
      metadata = undefined,
      sessionId = undefined,
      onboarding = () => {
      },
    } = options

    if (!flashcardSet.value) {
      Log.log(LogTag.SYSTEM, `Can't create review session ${sessionType}, FlashcardSet is undefined`)
      return Promise.resolve()
    }

    clear()

    if (sessionId) {
      const existingSession = await fetchReviewSession(flashcardSet.value, sessionId)
      if (!existingSession || existingSession.finishedAt) {
        await create({ elapsedTime, idsToTrack, metadata })
      } else {
        onboarding(existingSession)
        init(existingSession)
      }
    } else {
      await create({ elapsedTime, idsToTrack, metadata })
    }
  }

  return {
    get sessionId() { return reviewSession?.id },
    elapsedTime: sessionElapsedTime,
    init, create, loadOrCreate, flush, clear, track,
  }
}

export async function fetchReviewSession(flashcardSet: FlashcardSet, sessionId: number): Promise<ReviewSession | undefined> {
  return await sendReviewSessionGetRequest(flashcardSet.id, sessionId)
    .then((response) => {
      return response.data
    })
    .catch((error) => {
      Log.error(LogTag.LOGIC, `Failed to fetch review session ${sessionId}`, error.response?.data)
      useSpaceToaster().bakeError(userApiErrors.REVIEW_SESSION__FETCHING_FAILED, error.response?.data)
      return undefined
    })
}

export async function createReviewSession(flashcardSet: FlashcardSet, request: ReviewSessionCreateRequest): Promise<ReviewSession | undefined> {
  try {
    const response = await sendReviewSessionCreateRequest(flashcardSet.id, request)
    Log.log(LogTag.LOGIC, `Review session ${request.type}-${response.data.id} created`)
    return response.data
  } catch (error) {
    Log.error(LogTag.LOGIC, `Failed to create review session ${request.type}`, error)
    useSpaceToaster().bakeError(userApiErrors.REVIEW_SESSION__CREATION_FAILED, errorResponseData(error))
    return undefined
  }
}

export async function updateReviewSession(flashcardSet: FlashcardSet, sessionId: number, request: ReviewSessionUpdateRequest): Promise<ReviewSession | undefined> {
  try {
    const response = await sendReviewSessionUpdateRequest(flashcardSet.id, sessionId, request)
    Log.log(LogTag.LOGIC, `Review session ${sessionId} updated`)
    return response.data
  } catch (error) {
    Log.error(LogTag.LOGIC, `Failed to update review session ${sessionId}`, error)
    useSpaceToaster().bakeError(userApiErrors.REVIEW_SESSION__UPDATING_FAILED, errorResponseData(error))
    return undefined
  }
}
