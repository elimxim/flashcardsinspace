import { computed, readonly, Ref, ref } from 'vue'
import { Flashcard, FlashcardSet } from '@/model/flashcard.ts'
import { EmptyReviewQueue, ReviewQueue, ReviewSessionType } from '@/core-logic/review-logic.ts'
import { defineStore, getActivePinia } from 'pinia'
import {
  createMediaPrefetcher,
  FlashcardMediaPrefetcher,
} from '@/core-logic/flashcard-media-prefetch.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { userApiErrors } from '@/api/user-api-error.ts'
import { errorResponseData } from '@/core-logic/media-error.ts'
import { Log, LogTag } from '@/utils/logger.ts'

export type ReviewStore = ReturnType<typeof useReviewStore>

export const useReviewStore = (
  sessionType: ReviewSessionType,
  flashcardSet: Ref<FlashcardSet | undefined>,
) => {
  const storeId = buildStoreId(sessionType)
  return defineStore(storeId, () => {
    // state
    const reviewQueue = ref<ReviewQueue>(new EmptyReviewQueue())
    const flashcardsTotal = ref(0)
    const currFlashcard = ref<Flashcard>()
    const autoPlayVoice = ref(false)
    const autoRepeatVoice = ref(false)
    const flashcardFrontSideAudioBlob = ref<Blob>()
    const flashcardBackSideAudioBlob = ref<Blob>()
    const flashcardFrontSidePictureBlob = ref<Blob>()
    const flashcardBackSidePictureBlob = ref<Blob>()
    const loaded = ref(false)
    let prefetcher: FlashcardMediaPrefetcher | undefined = undefined

    // getters
    const flashcardsRemaining = computed(() => {
      if (currFlashcard.value === undefined) return 0
      return reviewQueue.value.remaining() + 1
    })

    const flashcardsSeen = computed(() =>
      Math.max(0, flashcardsTotal.value - flashcardsRemaining.value),
    )

    const noOneAvailable = computed(() => {
      return currFlashcard.value === undefined
    })

    const noPrevAvailable = computed(() => {
      if (flashcardsTotal.value === 1) {
        return currFlashcard.value !== undefined
      }
      return flashcardsTotal.value === flashcardsRemaining.value
    })

    const noNextAvailable = computed(() => {
      return reviewQueue.value.remaining() === 0
    })

    const progress = computed(() => {
      const completionRate = flashcardsSeen.value / flashcardsTotal.value
      if (completionRate) {
        return Math.max(0, Math.min(1, completionRate))
      } else {
        return 0
      }
    })

    // actions
    function loadState(queue: ReviewQueue) {
      $reset()
      queue.shuffle()
      reviewQueue.value = queue
      flashcardsTotal.value = queue.remaining()
      loaded.value = true
      if (flashcardSet.value) {
        Log.log(LogTag.DEBUG, `FlashcardSet ${flashcardSet.value.id} - creating media prefetcher`)
        prefetcher = createMediaPrefetcher(flashcardSet.value.id)
      } else {
        Log.log(LogTag.DEBUG, `Can't create media prefetcher, unknown FlashcardSet`)
      }
      // starts the first flashcards while the review session is still being created,
      // so even the very first one can be served from work already under way
      slidePrefetchWindow()
    }

    async function fetchMedia() {
      const flashcardId = currFlashcard.value?.id
      if (!prefetcher || !flashcardId) {
        clearMedia()
        return
      }

      const media = await prefetcher.media(flashcardId)
      // the user moved on while this was in flight
      if (currFlashcard.value?.id !== flashcardId) return

      flashcardFrontSideAudioBlob.value = media.frontAudio
      flashcardBackSideAudioBlob.value = media.backAudio
      flashcardFrontSidePictureBlob.value = media.frontPicture
      flashcardBackSidePictureBlob.value = media.backPicture

      if (media.error !== undefined) {
        useSpaceToaster().bakeError(
          userApiErrors.MEDIA__FETCHING_FAILED,
          errorResponseData(media.error),
        )
      }
    }

    async function refetchMedia() {
      const flashcardId = currFlashcard.value?.id
      if (flashcardId) prefetcher?.forget(flashcardId)
      await fetchMedia()
    }

    function clearMedia() {
      flashcardFrontSideAudioBlob.value = undefined
      flashcardBackSideAudioBlob.value = undefined
      flashcardFrontSidePictureBlob.value = undefined
      flashcardBackSidePictureBlob.value = undefined
    }

    function slidePrefetchWindow() {
      if (!prefetcher) return
      prefetcher.slide(
        currFlashcard.value,
        reviewQueue.value.lookahead(prefetcher.lookahead),
        reviewQueue.value.lookbehind(prefetcher.lookbehind),
      )
    }

    async function prevFlashcard(
      callback: (success: boolean) => void = () => {},
    ): Promise<boolean> {
      currFlashcard.value = reviewQueue.value.prev()
      await fetchMedia()
      const result = currFlashcard.value !== undefined
      callback(result)
      slidePrefetchWindow()
      return result
    }

    async function nextFlashcard(
      callback: (success: boolean) => void = () => {},
    ): Promise<boolean> {
      currFlashcard.value = reviewQueue.value.next()
      await fetchMedia()
      const result = currFlashcard.value !== undefined
      callback(result)
      slidePrefetchWindow()
      return result
    }

    function setFlashcardsTotal(total: number) {
      flashcardsTotal.value = total
    }

    function $reset() {
      prefetcher?.dispose()
      prefetcher = undefined
      reviewQueue.value = new EmptyReviewQueue()
      currFlashcard.value = undefined
      flashcardsTotal.value = 0
      autoPlayVoice.value = false
      autoRepeatVoice.value = false
      flashcardFrontSideAudioBlob.value = undefined
      flashcardBackSideAudioBlob.value = undefined
      flashcardFrontSidePictureBlob.value = undefined
      flashcardBackSidePictureBlob.value = undefined
      loaded.value = false
    }

    return {
      reviewStoreLoaded: readonly(loaded),
      reviewQueue,
      flashcardsTotal,
      currFlashcard,
      autoPlayVoice,
      autoRepeatVoice,
      flashcardFrontSideAudioBlob,
      flashcardBackSideAudioBlob,
      flashcardFrontSidePictureBlob,
      flashcardBackSidePictureBlob,

      flashcardsRemaining,
      flashcardsSeen,
      noOneAvailable,
      noPrevAvailable,
      noNextAvailable,
      progress,

      loadState,
      $reset,
      fetchMedia,
      refetchMedia,
      prevFlashcard,
      nextFlashcard,
      setFlashcardsTotal,
    }
  })()
}

function buildStoreId(sessionType: string) {
  return `review-${sessionType}`
}

export function destroyReviewStore(store: ReviewStore) {
  const storeId = store.$id
  store.$dispose()
  const pinia = getActivePinia()
  if (pinia) {
    delete pinia.state.value[storeId]
  }
}
