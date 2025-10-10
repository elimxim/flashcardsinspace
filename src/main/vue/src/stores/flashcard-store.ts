import { type Flashcard, type FlashcardSet } from '@/model/flashcard.ts'
import { defineStore } from 'pinia'
import type { Language } from '@/model/language.ts'
import { useLanguageStore } from '@/stores/language-store.ts'
import { flashcardSetStatuses } from '@/core-logic/flashcard-logic.ts'

export interface FlashcardSetState {
  flashcardSet: FlashcardSet | undefined
  flashcardMap: Map<number, Flashcard>
  loaded: boolean
}

export const useFlashcardStore = defineStore('flashcard', {
  state: (): FlashcardSetState => {
    return {
      flashcardSet: undefined,
      flashcardMap: new Map(),
      loaded: false,
    }
  },
  getters: {
    flashcards(): Flashcard[] {
      if (this.flashcardSet) {
        return [...this.flashcardMap.values()]
      } else {
        return []
      }
    },
    language(): Language | undefined {
      if (this.flashcardSet) {
        const languageStore = useLanguageStore()
        return languageStore.getLanguage(this.flashcardSet.languageId)
      }
    },
    isEmpty(): boolean {
      return this.flashcardMap.size === 0
    },
    isStarted(): boolean {
      return this.flashcardSet?.startedAt !== undefined
    },
    isSuspended(): boolean {
      return this.flashcardSet?.status === flashcardSetStatuses.SUSPENDED
    },
  },
  actions: {
    loadState(flashcardSet: FlashcardSet, flashcards: Flashcard[]) {
      console.log(`Loading flashcard set ${flashcardSet.id} with ${flashcards.length} flashcards`)
      this.resetState()
      this.flashcardSet = flashcardSet
      this.flashcardMap = new Map(flashcards.map(v => [v.id, v]))
      this.loaded = true
    },
    checkStateLoaded() {
      if (!this.flashcardSet) throw Error(`State check: flashcard set must be set`)
      if (!this.loaded) throw Error(`State check: flashcard store isn't loaded`)
    },
    resetState() {
      this.flashcardSet = undefined
      this.flashcardMap = new Map()
      this.loaded = false
    },
    changeSet(flashcardSet: FlashcardSet) {
      console.log(`Changing flashcard set ${flashcardSet.id}`)
      this.checkStateLoaded()
      if (this.flashcardSet?.id !== flashcardSet.id) {
        throw Error(`Can't change flashcard set: (current id) ${this.flashcardSet?.id} != ${flashcardSet.id} (new id)`)
      }
      this.flashcardSet = flashcardSet
    },
    addFlashcards(flashcards: Flashcard[]) {
      console.log(`Adding ${flashcards.length} flashcards to set ${this.flashcardSet?.id}`)
      this.checkStateLoaded()
      this.flashcardMap = new Map(flashcards.map(v => [v.id, v]))
    },
    addNewFlashcard(flashcard: Flashcard) {
      console.log(`Adding flashcard ${flashcard.id} to set ${this.flashcardSet?.id}`)
      this.checkStateLoaded()
      this.flashcardMap.set(flashcard.id, flashcard)
    },
    changeFlashcard(flashcard: Flashcard) {
      console.log(`Updating flashcard ${flashcard.id} in set ${this.flashcardSet?.id}`)
      this.checkStateLoaded()
      const currFlashcard = this.flashcardMap.get(flashcard.id) as Flashcard | undefined
      if (!currFlashcard) {
        console.error(`Couldn't find flashcard ${flashcard.id} in the store to update it`)
      } else if (currFlashcard.id !== flashcard.id) {
        console.error(`Flashcard id mismatch: ${currFlashcard.id} != ${flashcard.id}`)
      } else {
        this.flashcardMap.set(flashcard.id, flashcard)
      }
    },
    removeFlashcard(id: number) {
      this.flashcardMap.delete(id)
    },
  }
})
