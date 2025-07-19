import { type Flashcard, Level } from '@/models/flashcard';
import { defineStore } from 'pinia';
import { useFlashcardStateStore } from '@/stores/flashcard-state';
import { isStringBetween } from '@/utils/date.ts'

export const useReviewStateStore = defineStore('review-state', {
  state: () => {
    return {
      started: false,
      frontSide: true,
      reviewQueue: [] as Flashcard[],
      currFlashcard: undefined as Flashcard | undefined,
    }
  },
  actions: {
    startReview() {
      this.started = true
      this.frontSide = true
      this.initReviewQueue()
      this.nextFlashcard()
    },
    finishReview() {
      this.started = false
      this.frontSide = true
      this.reviewQueue = []
      this.currFlashcard = undefined
    },
    initReviewQueue() {
      // todo implement calendar logic
      const calendarFrom = new Date()
      const calendarTo = new Date()
      calendarTo.setDate(calendarTo.getDate() + 1)
      const calendarLevels: Level[] = [Level.FIRST, Level.SECOND, Level.THIRD, Level.FORTH, Level.FIFTH, Level.SIXTH, Level.SEVENTH]

      const stateStore = useFlashcardStateStore()
      const filteredFlashcards = stateStore.flashcards.filter(f =>
        calendarLevels.includes(f.level) && (f.reviewedAt === null || isStringBetween(f.reviewedAt, calendarFrom, calendarTo))
      )
      this.reviewQueue = [...filteredFlashcards]
    },
    nextFlashcard() {
      this.currFlashcard = this.reviewQueue.shift()
    },
    flipFlashcard() {
      this.frontSide = !this.frontSide
    },
    setFrontSide(value: boolean) {
      this.frontSide = value
    },
  }
})
