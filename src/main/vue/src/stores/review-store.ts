import { computed, ref } from 'vue'
import { Flashcard } from '@/model/flashcard.ts'
import { EmptyReviewQueue, ReviewQueue } from '@/core-logic/review-logic.ts'
import { defineStore } from 'pinia'

export const useReviewStore = (storeId: string) => {
  return defineStore(`review-${storeId}`, () => {
    // state
    const reviewQueue = ref<ReviewQueue>(new EmptyReviewQueue())
    const flashcardsTotal = ref(0)
    const currFlashcard = ref<Flashcard>()

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
    }

    return {
      reviewQueue,
      currFlashcard,
      flashcardsTotal,
      flashcardsRemaining,
      flashcardsSeen,
      noNextAvailable,
      noPrevAvailable,
      progress,
      resetState,
    }
  })()
}
