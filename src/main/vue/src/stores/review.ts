import { defineStore } from 'pinia'
import { useFlashcardSetStore } from '@/stores/flashcard-set.ts'
import { findFlashcardsForReview, flashcardsForStage } from '@/core-logic/review-logic.ts'
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
  reviewQueue: Flashcard[]
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
    startSpecialReview(stage: Stage) {
      this.settings.topic = stage.name
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
      this.reviewQueue = []
      this.currFlashcard = null
      this.editFormWasOpened = false
    },
    initReviewQueue() {
      const flashcardStateStore = useFlashcardSetStore()
      const flashcards = findFlashcardsForReview(flashcardStateStore.flashcards)
      this.reviewQueue = [...flashcards]
    },
    initStageReviewQueue(stage: Stage) {
      const flashcardStateStore = useFlashcardSetStore()
      const flashcards = flashcardsForStage(flashcardStateStore.flashcards, stage)
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
