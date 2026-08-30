import { Flashcard } from '@/model/flashcard.ts'
import { useAudioStore } from '@/stores/audio-store.ts'
import { usePictureStore } from '@/stores/picture-store.ts'
import { requestFlashcardAudioBlob } from '@/core-logic/flashcard-audio-logic.ts'
import { requestFlashcardPictureBlob } from '@/core-logic/flashcard-picture-logic.ts'
import { flashcardSides } from '@/core-logic/flashcard-logic.ts'
import { isCanceledError } from '@/core-logic/media-error.ts'
import { Log, LogTag } from '@/utils/logger.ts'

/**
 * How many upcoming flashcards are kept warm while the user reads the current one.
 * 0 turns the window off: media is then fetched when the flashcard is reached.
 */
export const PREFETCH_LOOKAHEAD = 4

/**
 * How many already-seen flashcards stay warm behind the current one, so stepping back
 * finds its media instead of queueing a fresh fetch.
 */
export const PREFETCH_LOOKBEHIND = 4

export interface FlashcardMedia {
  frontAudio: Blob | undefined
  backAudio: Blob | undefined
  frontPicture: Blob | undefined
  backPicture: Blob | undefined
  error: unknown
}

const NO_MEDIA: FlashcardMedia = {
  frontAudio: undefined,
  backAudio: undefined,
  frontPicture: undefined,
  backPicture: undefined,
  error: undefined,
}

export interface FlashcardMediaPrefetcher {
  readonly lookahead: number
  readonly lookbehind: number

  media(flashcardId: number): Promise<FlashcardMedia>

  slide(current: Flashcard | undefined, ahead: Flashcard[], behind: Flashcard[]): void

  forget(flashcardId: number): void

  dispose(): void

  size(): number
}

export function createMediaPrefetcher(
  flashcardSetId: number,
  lookahead: number = PREFETCH_LOOKAHEAD,
  lookbehind: number = PREFETCH_LOOKBEHIND,
): FlashcardMediaPrefetcher {
  const entries = new Map<number, Promise<FlashcardMedia>>()
  const controllers = new Map<number, AbortController>()
  let tail: Promise<unknown> = Promise.resolve()
  let disposed = false

  async function fetchMedia(flashcardId: number, signal: AbortSignal): Promise<FlashcardMedia> {
    if (disposed || signal.aborted) return NO_MEDIA

    try {
      const audioStore = useAudioStore()
      const pictureStore = usePictureStore()

      if (!audioStore.loaded || !pictureStore.loaded) return NO_MEDIA

      const audioId = (side: string) => audioStore.getAudioId(flashcardId, side)
      const pictureId = (side: string) => pictureStore.getPictureId(flashcardId, side)

      const audio = (side: string) => audioId(side)
        ? requestFlashcardAudioBlob(flashcardSetId, flashcardId, side, signal)
        : Promise.resolve(undefined)

      const picture = (side: string) => pictureId(side)
        ? requestFlashcardPictureBlob(flashcardSetId, flashcardId, side, signal)
        : Promise.resolve(undefined)

      const [frontAudio, backAudio, frontPicture, backPicture] = await Promise.all([
        audio(flashcardSides.FRONT),
        audio(flashcardSides.BACK),
        picture(flashcardSides.FRONT),
        picture(flashcardSides.BACK),
      ])

      Log.log(LogTag.SYSTEM, `Media fetched for Flashcard.id=${flashcardId}`, {
        frontAudio: frontAudio ? "yes" : "no",
        backAudio: backAudio ? "yes" : "no",
        frontPicture: frontPicture ? "yes" : "no",
        backPicture: backPicture ? "yes" : "no",
      })

      return {
        frontAudio: frontAudio,
        backAudio: backAudio,
        frontPicture: frontPicture,
        backPicture: backPicture,
        error: undefined,
      }
    } catch (error) {
      if (isCanceledError(error)) return NO_MEDIA
      Log.error(LogTag.SYSTEM, `Failed to fetch media for Flashcard.id=${flashcardId}`, error)
      return { ...NO_MEDIA, error: error }
    }
  }

  function entry(flashcardId: number): Promise<FlashcardMedia> {
    const existing = entries.get(flashcardId)
    if (existing) return existing

    const controller = new AbortController()
    controllers.set(flashcardId, controller)

    Log.log(LogTag.SYSTEM, `Queuing media fetch for Flashcard.id=${flashcardId}`)
    const promise = tail
      .then(() => fetchMedia(flashcardId, controller.signal))
      .finally(() => controllers.delete(flashcardId))

    tail = promise.catch((error) => {
      Log.error(LogTag.SYSTEM, `Media fetch rejected for Flashcard.id=${flashcardId}`, error)
    })

    entries.set(flashcardId, promise)
    return promise
  }

  function drop(flashcardId: number) {
    entries.delete(flashcardId)
    controllers.get(flashcardId)?.abort()
    controllers.delete(flashcardId)
  }

  return {
    lookahead: lookahead,
    lookbehind: lookbehind,

    media(flashcardId: number): Promise<FlashcardMedia> {
      if (disposed) return Promise.resolve(NO_MEDIA)
      return entry(flashcardId)
    },

    slide(current: Flashcard | undefined, ahead: Flashcard[], behind: Flashcard[]) {
      if (disposed) return

      const retained = new Set<number>()
      if (current) retained.add(current.id)
      ahead.forEach(flashcard => retained.add(flashcard.id))
      behind.forEach(flashcard => retained.add(flashcard.id))

      entries.forEach((_, flashcardId) => {
        if (!retained.has(flashcardId)) drop(flashcardId)
      })

      ahead.forEach(flashcard => entry(flashcard.id))
    },

    forget(flashcardId: number) {
      drop(flashcardId)
    },

    dispose() {
      disposed = true
      entries.clear()
      controllers.forEach(controller => controller.abort())
      controllers.clear()
    },

    size(): number {
      return entries.size
    },
  }
}
