import { defineStore } from 'pinia'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
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
  flashcardIndex: number
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
      flashcardIndex: 0,
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
    },
    reviewFinished(): boolean {
      return this.currFlashcard === null
    },
  },
  actions: {
    startReview() {
      this.settings.topic = 'Leitner'
      this.settings.mode = ReviewMode.LEITNER
      this.started = true
      this.isFrontSide = true
      this.flashcardIndex = -1
      this.initReviewQueue()
      this.nextFlashcard()
    },
    startSpecialReview(stage: Stage) {
      this.settings.topic = stage.displayName
      this.settings.mode = stage === specialStages.OUTER_SPACE ? ReviewMode.SPACE : ReviewMode.SPECIAL
      this.started = true
      this.isFrontSide = true
      this.flashcardIndex = -1
      this.initStageReviewQueue(stage)
      this.nextFlashcard()
    },
    finishReview() {
      this.settings.topic = ''
      this.settings.mode = ReviewMode.LEITNER
      this.started = false
      this.isFrontSide = true
      this.flashcardIndex = -1
      this.reviewQueue = []
      this.currFlashcard = null
      this.editFormWasOpened = false
    },
    initReviewQueue() {
      const flashcardSetStore = useFlashcardSetStore()
      const flashcards = findFlashcardsForReview(flashcardSetStore.flashcards)
      this.reviewQueue = [...flashcards]
    },
    initStageReviewQueue(stage: Stage) {
      const flashcardSetStore = useFlashcardSetStore()
      const flashcards = flashcardsForStage(flashcardSetStore.flashcards, stage)
      this.reviewQueue = [...flashcards]
    },
    prevFlashcard() {
      if (this.flashcardIndex <= 0) return
      this.currFlashcard = this.reviewQueue[--this.flashcardIndex] ?? null
    },
    nextFlashcard(): boolean {
      if (this.flashcardIndex >= this.reviewQueue.length) return false
      this.currFlashcard = this.reviewQueue[++this.flashcardIndex] ?? null
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
