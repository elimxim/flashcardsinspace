import { defineStore } from 'pinia'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import {
  createReviewQueue,
  createReviewQueueForStage,
  EmptyReviewQueue,
  ReviewQueue
} from '@/core-logic/review-logic.ts'
import { specialStages, type Stage } from '@/core-logic/stage-logic.ts'
import type { Flashcard } from '@/model/flashcard.ts'

export enum ReviewMode {
  LEITNER = 'LEITNER',
  SPECIAL = 'SPECIAL',
  SPACE = 'SPACE',
}

export interface ReviewSettings {
  topic: string
  mode: ReviewMode
}

export interface ReviewState {
  settings: ReviewSettings
  started: boolean
  isFrontSide: boolean
  reviewQueue: ReviewQueue
  currFlashcard: Flashcard | null
  editFormWasOpened: boolean
}

export const useReviewStore = defineStore('review', {
  state: (): ReviewState => {
    return {
      settings: {
        topic: '',
        mode: ReviewMode.LEITNER,
      },
      started: false,
      isFrontSide: true,
      reviewQueue: new EmptyReviewQueue(),
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
    },
    reviewFinished(): boolean {
      return this.currFlashcard === null
    },
    flashcardsRunningTotal(): number {
      return this.reviewQueue.runningTotal()
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
    startSpecialReview(stage: Stage) {
      this.settings.topic = stage.displayName
      this.settings.mode = stage === specialStages.OUTER_SPACE ? ReviewMode.SPACE : ReviewMode.SPECIAL
      this.started = true
      this.isFrontSide = true
      this.initStageReviewQueue(stage)
      this.nextFlashcard()
    },
    finishReview() {
      this.settings.topic = ''
      this.settings.mode = ReviewMode.LEITNER
      this.started = false
      this.isFrontSide = true
      this.reviewQueue = new EmptyReviewQueue()
      this.currFlashcard = null
      this.editFormWasOpened = false
    },
    initReviewQueue() {
      const flashcardSetStore = useFlashcardSetStore()
      this.reviewQueue = createReviewQueue(flashcardSetStore.flashcards)
    },
    initStageReviewQueue(stage: Stage) {
      const flashcardSetStore = useFlashcardSetStore()
      this.reviewQueue = createReviewQueueForStage(flashcardSetStore.flashcards, stage)
    },
    prevFlashcard(): boolean {
      this.currFlashcard = this.reviewQueue.prev()
      return this.currFlashcard !== null
    },
    nextFlashcard(): boolean {
      this.currFlashcard = this.reviewQueue.next()
      return this.currFlashcard !== null
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
