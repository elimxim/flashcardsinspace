import { type Flashcard, type FlashcardSet } from '@/model/flashcard.ts'
import { defineStore } from 'pinia'
import apiClient from '@/api/api-client.ts'
import type {
  FlashcardSetGetResponse,
  FlashcardPutRequest,
  FlashcardPutResponse,
  FlashcardSetPutRequest,
  FlashcardSetPutResponse,
  FlashcardsGetResponse,
  FlashcardsPostRequest,
  FlashcardsPostResponse,
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
      await apiClient.get<FlashcardSetGetResponse>('/flashcard-sets/' + id).then(response => {
        this.flashcardSet = response.data.flashcardSet
      }).catch(error => {
        console.log(error)
      })
    },
    async loadFlashcards(): Promise<void> {
      if (this.flashcardSet !== null) {
        await apiClient.get<FlashcardsGetResponse>('/flashcard-sets/' + this.flashcardSet.id + '/flashcards').then(response => {
          this.flashcardMap = new Map(response.data.flashcards.map(v => [v.id, v]))
          this.loaded = true
        }).catch(error => {
          console.error(error)
        })
      }
    },
    updateFlashcardSet(name: string | null, first: boolean | null) {
      if (this.flashcardSet !== null) {
        const request: FlashcardSetPutRequest = {
          flashcardSet: this.flashcardSet,
        }

        let changed = false
        if (name !== null) {
          request.flashcardSet.name = name
          changed = true
        }
        if (first !== null) {
          request.flashcardSet.default = first
          changed = true
        }

        if (changed) {
          apiClient.put<FlashcardSetPutResponse>('/flashcard-sets/' + this.flashcardSet.id, request).then(response => {
            if (this.flashcardSet !== null) {
              this.flashcardSet.name = response.data.flashcardSet.name
            }
          })
          // todo handle errors
        }
      } else {
        throw new Error(`Can't update flashcard set: it is null`)
      }
    },
    async addFlashcard(flashcard: Flashcard): Promise<void> {
      if (this.flashcardSet !== null) {
        const request: FlashcardsPostRequest = {
          flashcard: flashcard,
        }

        await apiClient.post<FlashcardsPostResponse>('/flashcard-sets/' + this.flashcardSet.id + '/flashcards', request).then(response => {
          console.log(`Added flashcard ${response.data.flashcard.id}`)
          this.flashcardMap.set(response.data.flashcard.id, response.data.flashcard)
        })
        // todo handle errors
      } else {
        console.error(`Can't add flashcard ${flashcard}: flashcard set is null`)
      }
    },
    updateFlashcard(value: Flashcard) {
      if (this.flashcardSet !== null) {
        const request: FlashcardPutRequest = {
          flashcard: value,
        }

        apiClient.put<FlashcardPutResponse>('/flashcard-sets/' + this.flashcardSet.id + '/flashcards/' + value.id, request).then(response => {
          const flashcard = this.flashcardMap.get(value.id) as Flashcard | undefined
          if (flashcard !== undefined) {
            flashcard.frontSide = response.data.flashcard.frontSide
            flashcard.backSide = response.data.flashcard.backSide
            flashcard.stage = response.data.flashcard.stage
            flashcard.reviewCount = response.data.flashcard.reviewCount
            flashcard.reviewHistory = response.data.flashcard.reviewHistory
            flashcard.lastUpdatedAt = response.data.flashcard.lastUpdatedAt
          }
        })
        // todo handle errors
      } else {
        throw new Error(`Can't update flashcard=${value}: flashcard set is null`)
      }
    },
    removeFlashcard(id: number) {
      if (this.flashcardSet !== null) {
        apiClient.delete('/flashcard-sets/' + this.flashcardSet.id + '/flashcards/' + id).then(_ => {
          this.flashcardMap.delete(id)
        })
        // todo handle errors
      } else {
        throw new Error(`Can't remove flashcard with id=${id}: flashcard set is null`)
      }
    },
    resetState() {
      this.flashcardSet = null
      this.flashcardMap = new Map()
      this.loaded = false
    },
  }
})
