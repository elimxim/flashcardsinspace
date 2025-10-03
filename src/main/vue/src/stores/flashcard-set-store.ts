import { type Flashcard, type FlashcardSet } from '@/model/flashcard.ts'
import { defineStore } from 'pinia'
import apiClient from '@/api/api-client.ts'
import type {
  FlashcardSetUpdateRequest,
} from '@/api/communication.ts'
import type { Language } from '@/model/language.ts'
import { useLanguageStore } from '@/stores/language-store.ts'
import { flashcardSetStatuses } from '@/core-logic/flashcard-logic.ts'

export interface FlashcardSetState {
  flashcardSet: FlashcardSet | null
  flashcardMap: Map<number, Flashcard>
  loaded: boolean
}

export const useFlashcardSetStore = defineStore('flashcard-set', {
  state: (): FlashcardSetState => {
    return {
      flashcardSet: null,
      flashcardMap: new Map(),
      loaded: false,
    }
  },
  getters: {
    flashcards(): Flashcard[] {
      if (this.flashcardSet !== null) {
        return [...this.flashcardMap.values()]
      } else {
        return []
      }
    },
    language(): Language | null {
      if (this.flashcardSet !== null) {
        const languageStore = useLanguageStore()
        return languageStore.getLanguage(this.flashcardSet.languageId)
      } else {
        return null
      }
    },
    isEmpty(): boolean {
      return this.flashcardMap.size === 0
    },
    isStarted(): boolean {
      return this.flashcardSet?.startedAt !== null
    },
    isSuspended(): boolean {
      return this.flashcardSet?.status === flashcardSetStatuses.SUSPENDED
    },
  },
  actions: {
    async loadFlashcardsFor(flashcardSet: FlashcardSet) {
      this.resetState()
      this.flashcardSet = flashcardSet
      await this.loadFlashcards()
    },
    async syncFlashcardSet(): Promise<void> {
      if (this.flashcardSet !== null) {
        return this.loadFlashcardSet(this.flashcardSet.id)
      }
    },
    async loadFlashcardSet(id: number): Promise<void> {
      await apiClient.get<FlashcardSet>('/flashcard-sets/' + id).then(response => {
        this.flashcardSet = response.data
      }).catch(error => {
        console.log(error)
      })
    },
    async loadFlashcards(): Promise<void> {
      if (this.flashcardSet !== null) {
        await apiClient.get<Flashcard[]>('/flashcard-sets/' + this.flashcardSet.id + '/flashcards').then(response => {
          this.flashcardMap = new Map(response.data.map(v => [v.id, v]))
          this.loaded = true
        }).catch(error => {
          console.error(error)
        })
      }
    },
    updateFlashcardSet(name?: string, language?: Language, first?: boolean) {
      if (this.flashcardSet !== null) {
        const request: FlashcardSetUpdateRequest = {
          name: name,
          languageId: language?.id,
          default: first,
        }
        apiClient.put<FlashcardSet>('/flashcard-sets/' + this.flashcardSet.id, request).then(response => {
          if (this.flashcardSet !== null) {
            this.flashcardSet = response.data
          }
        })
        // todo handle errors
      } else {
        throw new Error(`Can't update flashcard set: it is null`)
      }
    },
    async addFlashcard(flashcard: Flashcard): Promise<void> {
      if (this.flashcardSet !== null) {
        await apiClient.post<Flashcard>('/flashcard-sets/' + this.flashcardSet.id + '/flashcards', flashcard).then(response => {
          console.log(`Added flashcard ${response.data.id}`)
          this.flashcardMap.set(response.data.id, response.data)
        })
        // todo handle errors
      } else {
        console.error(`Can't add flashcard ${flashcard}: flashcard set is null`)
      }
    },
    updateFlashcard(updateFlashcard: Flashcard) {
      if (this.flashcardSet !== null) {
        apiClient.put<Flashcard>('/flashcard-sets/' + this.flashcardSet.id + '/flashcards/' + updateFlashcard.id, updateFlashcard).then(response => {
          const currFlashcard = this.flashcardMap.get(updateFlashcard.id) as Flashcard | undefined
          if (currFlashcard !== undefined) {
            currFlashcard.frontSide = response.data.frontSide
            currFlashcard.backSide = response.data.backSide
            currFlashcard.stage = response.data.stage
            currFlashcard.reviewCount = response.data.reviewCount
            currFlashcard.reviewHistory = response.data.reviewHistory
            currFlashcard.lastUpdatedAt = response.data.lastUpdatedAt
          }
        })
        // todo handle errors
      } else {
        throw new Error(`Can't update flashcard=${updateFlashcard}: flashcard set is null`)
      }
    },
    removeFlashcard(id: number) {
      this.flashcardMap.delete(id)
    },
    resetState() {
      this.flashcardSet = null
      this.flashcardMap = new Map()
      this.loaded = false
    },
  }
})
