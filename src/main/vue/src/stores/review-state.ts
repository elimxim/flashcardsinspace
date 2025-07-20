import { type Flashcard, Level } from '@/models/flashcard';
import { defineStore } from 'pinia';
import { useFlashcardStateStore } from '@/stores/flashcard-state';
import { isStringBefore, isStringBetween } from '@/utils/date.ts'

export const useReviewStateStore = defineStore('review-state', {
  state: () => {
    return {
      started: false,
      isFrontSide: true,
      reviewQueue: [] as Flashcard[],
      currFlashcard: undefined as Flashcard | undefined,
      editFormWasOpened: false,
    }
  },
  getters: {
    frontSide(): string {
      if (this.currFlashcard === undefined) return ''
      return this.currFlashcard.frontSide
    },
    backSide(): string {
      if (this.currFlashcard === undefined) return ''
      return this.currFlashcard.backSide
    }
  },
  actions: {
    startReview() {
      this.started = true
      this.isFrontSide = true
      this.initReviewQueue()
      this.nextFlashcard()
    },
    finishReview() {
      this.started = false
      this.isFrontSide = true
      this.reviewQueue = []
      this.currFlashcard = undefined
    },
    initReviewQueue() {
      // todo implement calendar logic
      const calendarFrom = new Date()
      const calendarTo = new Date()
      calendarTo.setDate(calendarTo.getDate() + 1)
      const calendarLevels: Level[] = [Level.FIRST, Level.SECOND, Level.THIRD, Level.FORTH, Level.FIFTH, Level.SIXTH, Level.SEVENTH]
      const breakCalendarRules = true

      const stateStore = useFlashcardStateStore()
      const filteredFlashcards = stateStore.flashcards
        .filter(f => breakCalendarRules || calendarLevels.includes(f.level))
        .filter(f => breakCalendarRules || (
            // the flashcard was reviewed before the beginning of the current calendar day
            (f.reviewedAt !== null && isStringBefore(f.reviewedAt, calendarFrom) ||
              // or the flashcard wasn't reviewed yet but was created before the beginning of the current calendar day
              (f.reviewedAt === null && isStringBefore(f.createdAt, calendarFrom)))
          )
        )
      // todo sort
      this.reviewQueue = [...filteredFlashcards]
    },
    isNoCardsForReview() {
      return this.currFlashcard === undefined
    },
    nextFlashcard() {
      this.currFlashcard = this.reviewQueue.shift()
    },
    flipFlashcard() {
      this.isFrontSide = !this.isFrontSide
    },
    setFrontSide(value: boolean) {
      this.isFrontSide = value
    },
    setEditFormWasOpened(value: boolean) {
      this.editFormWasOpened = value
    },
  }
})
