import { type FlashcardSet } from '@/model/flashcard.ts'
import { defineStore } from 'pinia'

export interface FlashcardSetsState {
  flashcardSets: FlashcardSet[]
  loaded: boolean
}

export const useFlashcardSetsStore = defineStore('flashcard-sets', {
  state: (): FlashcardSetsState => {
    return {
      flashcardSets: [],
      loaded: false,
    }
  },
  getters: {
    isEmpty(): boolean {
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
    loadState(flashcardSets: FlashcardSet[]) {
      this.resetState()
      this.flashcardSets = flashcardSets
      this.loaded = true
    },
    checkStateLoaded() {
      if (!this.loaded) throw Error(`State check: flashcard sets store isn't loaded`)
    },
    resetState() {
      this.flashcardSets = []
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
      const idx = this.flashcardSets.indexOf(flashcardSet, 0)
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
  }
})
