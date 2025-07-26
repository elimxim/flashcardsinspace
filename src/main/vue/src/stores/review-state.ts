import { defineStore } from 'pinia'
import { useFlashcardStateStore } from '@/stores/flashcard-state'
import { ReviewMode, type ReviewState } from '@/models/state.ts'
import { flashcardsForStage, findFlashcardsForReview } from '@/core-logic/review-logic.ts'
import { type Stage, stages } from '@/core-logic/stage-logic.ts'

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
    startSpecialReview(stage: Stage) {
      this.settings.topic = stage.name
      this.settings.mode = stage === stages.OUTER_SPACE ? ReviewMode.SPACE : ReviewMode.SPECIAL
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
      const flashcardStateStore = useFlashcardStateStore()
      const flashcards = findFlashcardsForReview(flashcardStateStore.flashcards)
      this.reviewQueue = [...flashcards]
    },
    initStageReviewQueue(stage: Stage) {
      const flashcardStateStore = useFlashcardStateStore()
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
