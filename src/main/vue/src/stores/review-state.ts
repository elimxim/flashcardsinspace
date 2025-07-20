import { type Flashcard, Level } from '@/models/flashcard'
import { defineStore } from 'pinia'
import { useFlashcardStateStore } from '@/stores/flashcard-state'
import { isStringBefore } from '@/utils/date.ts'
import type { ReviewState } from '@/models/store.ts'

export const useReviewStateStore = defineStore('review-state', {
  state: (): ReviewState => {
    return {
      started: false,
      isFrontSide: true,
      reviewQueue: [],
      currFlashcard: null,
      editFormWasOpened: false,
    }
  },
  getters: {
    frontSide(): string {
      return this.currFlashcard?.frontSide ?? ''
    },
    backSide(): string {
      return this.currFlashcard?.backSide ?? ''
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
      this.currFlashcard = null
      this.editFormWasOpened = false
    },
    initReviewQueue() {
      // todo implement calendar logic
      const calendarFrom = new Date()
      const calendarTo = new Date()
      calendarTo.setDate(calendarTo.getDate() + 1)
      const calendarLevels: Level[] = [Level.FIRST, Level.SECOND, Level.THIRD, Level.FORTH, Level.FIFTH, Level.SIXTH, Level.SEVENTH]
      const breakCalendarRules = true

      const flashcardStateStore = useFlashcardStateStore()
      const filteredFlashcards = flashcardStateStore.flashcards
        .filter(f => breakCalendarRules || calendarLevels.includes(f.level))
        .filter(f => breakCalendarRules || (
            // the flashcard was reviewed before the beginning of the current calendar day
            (f.reviewedAt !== null && isStringBefore(f.reviewedAt, calendarFrom) ||
              // or the flashcard wasn't reviewed yet but was created before the beginning of the current calendar day
              (f.reviewedAt === null && isStringBefore(f.createdAt, calendarFrom)))
          )
        ).sort((a, b) => a.id - b.id)

      this.reviewQueue = [...filteredFlashcards]
    },
    isNoCardsForReview() {
      return this.currFlashcard === null
    },
    nextFlashcard() {
      this.currFlashcard = this.reviewQueue.shift() ?? null
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
