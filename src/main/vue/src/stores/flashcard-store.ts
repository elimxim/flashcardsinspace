import { type Flashcard, type FlashcardSet } from '@/model/flashcard.ts'
import { defineStore } from 'pinia'
import type { Language } from '@/model/language.ts'
import { useLanguageStore } from '@/stores/language-store.ts'
import { flashcardSetStatuses } from '@/core-logic/flashcard-logic.ts'
import { Log, LogTag } from '@/utils/logger.ts'

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
      const r = this.flashcardSet?.startedAt !== undefined
      return r
    },
    isSuspended(): boolean {
      return this.flashcardSet?.status === flashcardSetStatuses.SUSPENDED
    },
  },
  actions: {
    loadState(flashcardSet: FlashcardSet, flashcards: Flashcard[]) {
      Log.log(LogTag.STORE, `flashcard.loadState: FlashcardSet.id=${flashcardSet.id} + ${flashcards.length} flashcards`)
      this.resetState()
      this.flashcardSet = flashcardSet
      this.flashcardMap = new Map(flashcards.map(v => [v.id, v]))
      this.loaded = true
    },
    checkStateLoaded() {
      if (!this.flashcardSet) throw Error(`flashcard.checkStateLoaded: !flashcardSet`)
      if (!this.loaded) throw Error(`flashcard.checkStateLoaded: !loaded`)
    },
    resetState() {
      this.flashcardSet = undefined
      this.flashcardMap = new Map()
      this.loaded = false
    },
    changeSet(flashcardSet: FlashcardSet) {
      Log.log(LogTag.STORE, `flashcard.changeSet: FlashcardSet.id=${flashcardSet.id}`)
      this.checkStateLoaded()
      if (this.flashcardSet?.id !== flashcardSet.id) {
        throw Error(`flashcard.changeSet: (current FlashcardSet.id) ${this.flashcardSet?.id} != ${flashcardSet.id} (new FlashcardSet.id)`)
      }
      this.flashcardSet = flashcardSet
    },
    addFlashcards(flashcards: Flashcard[]) {
      Log.log(LogTag.STORE, `flashcard.addFlashcards: FlashcardSet.id=${this.flashcardSet?.id} + ${flashcards.length} flashcards`)
      this.checkStateLoaded()
      this.flashcardMap = new Map(flashcards.map(v => [v.id, v]))
    },
    addNewFlashcard(flashcard: Flashcard) {
      Log.log(LogTag.STORE, `flashcard.addNewFlashcard: FlashcardSet.id=${this.flashcardSet?.id} + Flashcard.id=${flashcard.id}`)
      this.checkStateLoaded()
      this.flashcardMap.set(flashcard.id, flashcard)
    },
    changeFlashcard(flashcard: Flashcard) {
      Log.log(LogTag.STORE, `flashcard.changeFlashcard: FlashcardSet.id=${this.flashcardSet?.id} + Flashcard.id=${flashcard.id}`)
      this.checkStateLoaded()
      const currFlashcard = this.flashcardMap.get(flashcard.id) as Flashcard | undefined
      if (!currFlashcard) {
        Log.error(LogTag.STORE, `flashcard.changeFlashcard: Flashcard.id=${flashcard.id} not found`)
      } else if (currFlashcard.id !== flashcard.id) {
        Log.error(LogTag.STORE, `flashcard.changeFlashcard: (current Flashcard.id) ${currFlashcard.id} != ${flashcard.id} (new Flashcard.id)`)
      } else {
        this.flashcardMap.set(flashcard.id, flashcard)
      }
    },
    removeFlashcard(id: number) {
      this.flashcardMap.delete(id)
    },
  }
})
