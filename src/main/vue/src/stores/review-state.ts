import { defineStore } from 'pinia'
import { useFlashcardStateStore } from '@/stores/flashcard-state'
import { ReviewMode, type ReviewState } from '@/models/state.ts'
import { flashcardsForLevel, flashcardsForReview } from '@/core-logic/review-logic.ts'
import { type Level, levels } from '@/core-logic/level-logic.ts'

export const useReviewStateStore = defineStore('review-state', {
  state: (): ReviewState => {
    return {
      settings: {
        topic: '',
        mode: ReviewMode.LEITNER,
      },
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
      this.settings.topic = 'Leitner'
      this.settings.mode = ReviewMode.LEITNER
      this.started = true
      this.isFrontSide = true
      this.initReviewQueue()
      this.nextFlashcard()
    },
    startSpecialReview(level: Level) {
      this.settings.topic = level.name
      this.settings.mode = level === levels.OUTER_SPACE ? ReviewMode.SPACE : ReviewMode.SPECIAL
      this.started = true
      this.isFrontSide = true
      this.initLevelReviewQueue(level)
      this.nextFlashcard()
    },
    finishReview() {
      this.settings.topic = ''
      this.settings.mode = ReviewMode.LEITNER
      this.started = false
      this.isFrontSide = true
      this.reviewQueue = []
      this.currFlashcard = null
      this.editFormWasOpened = false
    },
    initReviewQueue() {
      const flashcardStateStore = useFlashcardStateStore()
      const flashcards = flashcardsForReview(flashcardStateStore.flashcards)
      this.reviewQueue = [...flashcards]
    },
    initLevelReviewQueue(level: Level) {
      const flashcardStateStore = useFlashcardStateStore()
      const flashcards = flashcardsForLevel(flashcardStateStore.flashcards, level)
      this.reviewQueue = [...flashcards]
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
