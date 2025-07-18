import type { Flashcard } from '@/models/flashcard';
import { defineStore, storeToRefs } from 'pinia';
import { useFlashcardStateStore } from './flashcard-state';

export const useReviewStateStore = defineStore('review-state', {
  state: () => {
    return {
      started: false,
      frontSide: true,
      queue: [] as Flashcard[],
      currFlashcard: undefined as Flashcard | undefined,
    }
  },
  actions: {
    startReview() {
      this.started = true
      this.frontSide = true
      this.createQueue()
      this.nextFlashcard()
    },
    finishReview() {
      this.started = false
      this.frontSide = true
      this.queue = []
      this.currFlashcard = undefined
    },
    createQueue() {
      const stateStore = useFlashcardStateStore()
      // todo define the queue using calendar
      this.queue = [...stateStore.flashcards]
    },
    nextFlashcard() {
      this.currFlashcard = this.queue.shift()
    },
    flipFlashcard() {
      this.frontSide = !this.frontSide
    },
  }
})
