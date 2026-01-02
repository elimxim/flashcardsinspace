import { computed, ref } from 'vue'
import { Flashcard, FlashcardSet } from '@/model/flashcard.ts'
import { EmptyReviewQueue, ReviewQueue } from '@/core-logic/review-logic.ts'
import { defineStore } from 'pinia'
import { fetchFlashcardAudio } from '@/core-logic/flashcard-logic.ts'

export const useReviewStore = (sessionType: string) => {
  const storeId = `review-${sessionType}`
  return defineStore(storeId, () => {
    // state
    const reviewQueue = ref<ReviewQueue>(new EmptyReviewQueue())
    const flashcardsTotal = ref(0)
    const currFlashcard = ref<Flashcard>()
    const autoPlayVoice = ref(false)
    const autoRepeatVoice = ref(false)
    const flashcardFrontSideAudioBlob = ref<Blob>()
    const flashcardBackSideAudioBlob = ref<Blob>()

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
    function resetState() {
      reviewQueue.value = new EmptyReviewQueue()
      currFlashcard.value = undefined
      flashcardsTotal.value = 0
      autoPlayVoice.value = false
      autoRepeatVoice.value = false
      flashcardFrontSideAudioBlob.value = undefined
      flashcardBackSideAudioBlob.value = undefined
    }

    async function fetchAudio(flashcardSet: FlashcardSet | undefined) {
      await fetchFlashcardAudio(
        flashcardSet?.id,
        currFlashcard.value?.id,
        flashcardFrontSideAudioBlob,
        flashcardBackSideAudioBlob,
      )
    }

    async function prevFlashcard(flashcardSet: FlashcardSet | undefined): Promise<boolean> {
      currFlashcard.value = reviewQueue.value.prev()
      await fetchAudio(flashcardSet)
      return currFlashcard.value !== undefined
    }

    async function nextFlashcard(flashcardSet: FlashcardSet | undefined): Promise<boolean> {
      currFlashcard.value = reviewQueue.value.next()
      await fetchAudio(flashcardSet)
      return currFlashcard.value !== undefined
    }

    return {
      reviewQueue,
      flashcardsTotal,
      currFlashcard,
      autoPlayVoice,
      autoRepeatVoice,
      flashcardFrontSideAudioBlob,
      flashcardBackSideAudioBlob,

      flashcardsRemaining,
      flashcardsSeen,
      noNextAvailable,
      noPrevAvailable,
      progress,

      getStoreId: () => storeId,
      resetState,
      fetchAudio,
      prevFlashcard,
      nextFlashcard,
    }
  })()
}
