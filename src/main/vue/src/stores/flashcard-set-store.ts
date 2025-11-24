import { type FlashcardSet, FlashcardSetExtra } from '@/model/flashcard.ts'
import { defineStore } from 'pinia'
import { mapFlashcardSetExtra } from '@/core-logic/flashcard-logic.ts'

export interface FlashcardSetsState {
  flashcardSets: FlashcardSet[]
  extra: Map<number, FlashcardSetExtra>
  loaded: boolean
}

export const useFlashcardSetStore = defineStore('flashcard-set', {
  state: (): FlashcardSetsState => {
    return {
      flashcardSets: [],
      extra: new Map(),
      loaded: false,
    }
  },
  getters: {
    isNoFlashcardSets(): boolean {
      return this.flashcardSets.length === 0
    },
    firstFlashcardSet(): FlashcardSet | undefined {
      return this.flashcardSets[0]
    },
    lastFlashcardSet(): FlashcardSet | undefined {
      return this.flashcardSets[this.flashcardSets.length - 1]
    },
  },
  actions: {
    loadState(flashcardSets: FlashcardSet[], extras: FlashcardSetExtra[]) {
      this.resetState()
      this.flashcardSets = flashcardSets
      this.extra = mapFlashcardSetExtra(extras)
      this.loaded = true
    },
    checkStateLoaded() {
      if (!this.loaded) throw Error(`State check: flashcard set store isn't loaded`)
    },
    resetState() {
      this.flashcardSets = []
      this.extra = new Map()
      this.loaded = false
    },
    addSet(flashcardSet: FlashcardSet) {
      console.log(`Adding flashcard set ${flashcardSet.id}`)
      this.checkStateLoaded()
      this.flashcardSets.push(flashcardSet)
    },
    removeSet(flashcardSet: FlashcardSet) {
      console.log(`Removing flashcard set ${flashcardSet.id}`)
      this.checkStateLoaded()
      const idx = this.flashcardSets.findIndex((v: FlashcardSet) => v.id == flashcardSet.id)
      if (idx !== -1) {
        this.flashcardSets.splice(idx, 1)
      } else {
        throw Error(`Couldn't find flashcard set ${flashcardSet.id} in the store to remove`)
      }
    },
    updateSet(flashcardSet: FlashcardSet) {
      console.log(`Updating flashcard set ${flashcardSet.id}`)
      this.checkStateLoaded()
      const idx = this.flashcardSets.findIndex((v: FlashcardSet) => v.id == flashcardSet.id)
      if (idx !== -1) {
        this.flashcardSets[idx] = flashcardSet
      } else {
        throw Error(`Couldn't find flashcard set ${flashcardSet.id} in the store to update`)
      }
    },
    findSet(id: number): FlashcardSet | undefined {
      return this.flashcardSets.find(v => v.id === id)
    },
    addExtra(id: number) {
      if (!this.extra.has(id)) {
        this.extra.set(id, { id: id, flashcardsNumber: 0 })
      }
    },
    findExtra(id: number): FlashcardSetExtra | undefined {
      return this.extra.get(id)
    },
    incrementFlashcardsNumber(id: number) {
      const extra = this.findExtra(id)
      if (extra) {
        extra.flashcardsNumber += 1
      }
    },
    decrementFlashcardsNumber(id: number) {
      const extra = this.findExtra(id)
      if (extra) {
        extra.flashcardsNumber -= 1
      }
    },
  }
})
