import { computed, readonly, ref } from 'vue'
import { Flashcard, FlashcardSet } from '@/model/flashcard.ts'
import { EmptyReviewQueue, ReviewQueue, ReviewSessionType } from '@/core-logic/review-logic.ts'
import { defineStore, getActivePinia } from 'pinia'
import { fetchFlashcardAudio } from '@/core-logic/flashcard-audio-logic.ts'
import { fetchFlashcardPicture } from '@/core-logic/flashcard-picture-logic.ts'

export const useReviewStore = (sessionType: ReviewSessionType) => {
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

    // getters
    const flashcardsRemaining = computed(() => {
      if (noNextAvailable.value) return 0
      return reviewQueue.value.remaining() + 1
    })

    const flashcardsSeen = computed(() =>
      Math.max(0, flashcardsTotal.value - flashcardsRemaining.value)
    )

    const noNextAvailable = computed(() => {
      return currFlashcard.value === undefined
    })

    const noPrevAvailable = computed(() => {
      if (flashcardsTotal.value === 1) {
        return !noNextAvailable.value
      }
      return flashcardsTotal.value === flashcardsRemaining.value
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
    }

    async function fetchAudio(flashcardSet: FlashcardSet | undefined) {
      await fetchFlashcardAudio(
        flashcardSet?.id,
        currFlashcard.value?.id,
        flashcardFrontSideAudioBlob,
        flashcardBackSideAudioBlob,
      )
    }

    async function fetchPicture(flashcardSet: FlashcardSet | undefined) {
      await fetchFlashcardPicture(
        flashcardSet?.id,
        currFlashcard.value?.id,
        flashcardFrontSidePictureBlob,
        flashcardBackSidePictureBlob,
      )
    }

    async function fetchMedia(flashcardSet: FlashcardSet | undefined) {
      await Promise.all([
        fetchAudio(flashcardSet),
        fetchPicture(flashcardSet),
      ])
    }

    async function prevFlashcard(
      flashcardSet: FlashcardSet | undefined,
      callback: (success: boolean) => void = () => {},
    ): Promise<boolean> {
      currFlashcard.value = reviewQueue.value.prev()
      await fetchMedia(flashcardSet)
      const result = currFlashcard.value !== undefined
      callback(result)
      return result
    }

    async function nextFlashcard(
      flashcardSet: FlashcardSet | undefined,
      callback: (success: boolean) => void = () => {},
    ): Promise<boolean> {
      currFlashcard.value = reviewQueue.value.next()
      await fetchMedia(flashcardSet)
      const result = currFlashcard.value !== undefined
      callback(result)
      return result
    }

    function setFlashcardsTotal(total: number) {
      flashcardsTotal.value = total
    }

    function $reset() {
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
      noNextAvailable,
      noPrevAvailable,
      progress,

      loadState,
      $reset,
      fetchAudio,
      fetchPicture,
      fetchMedia,
      prevFlashcard,
      nextFlashcard,
      setFlashcardsTotal,
    }
  })()
}

function buildStoreId(sessionType: string){
  return `review-${sessionType}`
}

export function destroyReviewStore(sessionType: ReviewSessionType) {
  const storeId = buildStoreId(sessionType)
  const pinia = getActivePinia()
  if (pinia) {
    delete pinia.state.value[storeId]
  }
}
