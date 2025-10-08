import { defineStore } from 'pinia'
import {
  createReviewQueue,
  createReviewQueueForStage,
  EmptyReviewQueue,
  ReviewQueue,
} from '@/core-logic/review-logic.ts'
import { type Stage } from '@/core-logic/stage-logic.ts'
import type { Flashcard } from '@/model/flashcard.ts'

export interface ReviewSettings {
  topic: string
}

export interface ReviewState {
  settings: ReviewSettings
  reviewQueue: ReviewQueue
  currFlashcard: Flashcard | null
  loaded: boolean
}

export const useReviewStore = defineStore('review', {
  state: (): ReviewState => {
    return {
      settings: {
        topic: '',
      },
      reviewQueue: new EmptyReviewQueue(),
      currFlashcard: null,
      loaded: false,
    }
  },
  getters: {
    reviewFinished(): boolean {
      return this.currFlashcard === null
    },
    remainingFlashcards(): number {
      return this.reviewQueue.remaining() + 1
    }
  },
  actions: {
    startReview(flashcards: Flashcard[], stage: Stage | undefined) {
      this.resetState()
      this.settings.topic = stage !== undefined ? stage.displayName : 'Lightspeed'
      if (stage !== undefined) {
        this.reviewQueue = createReviewQueueForStage(flashcards, stage)
      } else {
        this.reviewQueue = createReviewQueue(flashcards)
      }
      this.nextFlashcard()
      this.loaded = true
    },
    finishReview() {
      this.resetState()
    },
    prevFlashcard(): boolean {
      this.currFlashcard = this.reviewQueue.prev()
      return this.currFlashcard !== null
    },
    nextFlashcard(): boolean {
      this.currFlashcard = this.reviewQueue.next()
      return this.currFlashcard !== null
    },
    resetState() {
      this.settings.topic = ''
      this.reviewQueue = new EmptyReviewQueue()
      this.currFlashcard = null
      this.loaded = false
    },
  }
})
