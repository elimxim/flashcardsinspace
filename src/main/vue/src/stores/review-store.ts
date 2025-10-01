import { defineStore } from 'pinia'
import {
  createReviewQueue,
  createReviewQueueForStage,
  EmptyReviewQueue,
  ReviewMode,
  ReviewQueue,
  toReviewMode
} from '@/core-logic/review-logic.ts'
import { type Stage } from '@/core-logic/stage-logic.ts'
import type { Flashcard } from '@/model/flashcard.ts'

export interface ReviewSettings {
  topic: string
  mode: ReviewMode
}

export interface ReviewState {
  settings: ReviewSettings
  started: boolean
  reviewQueue: ReviewQueue
  currFlashcard: Flashcard | null
  editFormWasOpened: boolean
  loaded: boolean
}

export const useReviewStore = defineStore('review', {
  state: (): ReviewState => {
    return {
      settings: {
        topic: '',
        mode: ReviewMode.LIGHTSPEED,
      },
      started: false,
      reviewQueue: new EmptyReviewQueue(),
      currFlashcard: null,
      editFormWasOpened: false,
      loaded: false,
    }
  },
  getters: {
    frontSide(): string {
      return this.currFlashcard?.frontSide ?? ''
    },
    backSide(): string {
      return this.currFlashcard?.backSide ?? ''
    },
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
      this.settings.mode = toReviewMode(stage)
      this.started = true
      if (stage !== undefined) {
        this.initStageReviewQueue(flashcards, stage)
      } else {
        this.initReviewQueue(flashcards)
      }
      this.nextFlashcard()
      this.loaded = true
    },
    finishReview() {
      this.resetState()
    },
    initReviewQueue(flashcards: Flashcard[]) {
      this.reviewQueue = createReviewQueue(flashcards)
    },
    initStageReviewQueue(flashcards: Flashcard[], stage: Stage) {
      this.reviewQueue = createReviewQueueForStage(flashcards, stage)
    },
    prevFlashcard(): boolean {
      this.currFlashcard = this.reviewQueue.prev()
      return this.currFlashcard !== null
    },
    nextFlashcard(): boolean {
      this.currFlashcard = this.reviewQueue.next()
      return this.currFlashcard !== null
    },
    setEditFormWasOpened(value: boolean) {
      this.editFormWasOpened = value
    },
    resetState() {
      this.settings.topic = ''
      this.settings.mode = ReviewMode.LIGHTSPEED
      this.started = false
      this.reviewQueue = new EmptyReviewQueue()
      this.currFlashcard = null
      this.editFormWasOpened = false
      this.loaded = false
    },
  }
})
