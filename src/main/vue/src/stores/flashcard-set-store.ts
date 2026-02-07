import { type FlashcardSet, FlashcardSetExtra } from '@/model/flashcard.ts'
import { defineStore } from 'pinia'
import { mapFlashcardSetExtra } from '@/core-logic/flashcard-logic.ts'
import { Log, LogTag } from '@/utils/logger.ts'

export interface FlashcardSetsState {
  flashcardSets: FlashcardSet[]
  extra: Map<number, FlashcardSetExtra>
  loaded: boolean
  extraLoaded: boolean
}

export const useFlashcardSetStore = defineStore('flashcard-set', {
  state: (): FlashcardSetsState => {
    return {
      flashcardSets: [],
      extra: new Map(),
      loaded: false,
      extraLoaded: false,
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
    loadState(flashcardSets: FlashcardSet[]) {
      this.$reset()
      this.flashcardSets = flashcardSets
      this.loaded = true
    },
    checkStateLoaded() {
      if (!this.loaded) throw Error(`flashcard-set.checkStateLoaded: !loaded`)
    },
    loadExtras(extras: FlashcardSetExtra[]) {
      this.extra = mapFlashcardSetExtra(extras)
      this.extraLoaded = true
    },
    addSet(flashcardSet: FlashcardSet) {
      Log.log(LogTag.STORE, `flashcard-set.addSet: FlashcardSet.id=${flashcardSet.id}`)
      this.checkStateLoaded()
      this.flashcardSets.push(flashcardSet)
    },
    removeSet(flashcardSet: FlashcardSet) {
      Log.log(LogTag.STORE, `flashcard-set.removeSet: FlashcardSet.id=${flashcardSet.id}`)
      this.checkStateLoaded()
      const idx = this.flashcardSets.findIndex((v: FlashcardSet) => v.id == flashcardSet.id)
      if (idx !== -1) {
        this.flashcardSets.splice(idx, 1)
      } else {
        throw Error(`flashcard-set.removeSet: FlashcardSet.id=${flashcardSet.id} not found`)
      }
    },
    updateSet(flashcardSet: FlashcardSet) {
      Log.log(LogTag.STORE, `flashcard-set.updateSet: FlashcardSet.id=${flashcardSet.id}`)
      this.checkStateLoaded()
      const idx = this.flashcardSets.findIndex((v: FlashcardSet) => v.id == flashcardSet.id)
      if (idx !== -1) {
        this.flashcardSets[idx] = flashcardSet
      } else {
        throw Error(`flashcard-set.updateSet: FlashcardSet.id=${flashcardSet.id} not found`)
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
    getExtra(id: number): FlashcardSetExtra | undefined {
      return this.extra.get(id)
    },
    incrementFlashcardsNumber(id: number) {
      const extra = this.getExtra(id)
      if (extra) {
        extra.flashcardsNumber += 1
      }
    },
    decrementFlashcardsNumber(id: number) {
      const extra = this.getExtra(id)
      if (extra) {
        extra.flashcardsNumber -= 1
      }
    },
  }
})
