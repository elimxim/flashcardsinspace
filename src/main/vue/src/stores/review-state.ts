import { defineStore } from 'pinia'
import { useFlashcardStateStore } from '@/stores/flashcard-state'
import type { ReviewState } from '@/models/store.ts'
import { flashcardsForLevel, flashcardsForReview } from '@/core-logic/review-logic.ts'

import type { Level } from '@/core-logic/level-logic.ts';

export const useReviewStateStore = defineStore('review-state', {
  state: (): ReviewState => {
    return {
      started: false,
      topic: '',
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
    startReview(topic: string) {
      this.started = true
      this.topic = topic
      this.isFrontSide = true
    },
    finishReview() {
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
