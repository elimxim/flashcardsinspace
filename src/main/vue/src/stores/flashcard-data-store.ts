import { type FlashcardSet } from '@/model/flashcard.ts'
import { defineStore } from 'pinia'
import apiClient from '@/api/api-client.ts'
import type {
  FlashcardSetsPostRequest,
  FlashcardSetsPostResponse,
  FlashcardSetsGetResponse
} from '@/api/communication.ts'

export interface FlashcardDataState {
  flashcardSets: FlashcardSet[]
}

export const useFlashcardDataStore = defineStore('flashcard-data', {
  state: (): FlashcardDataState => {
    return {
      flashcardSets: [],
    }
  },
  getters: {
    isEmpty(): boolean {
      return this.flashcardSets.length === 0
    },
    firstFlashcardSet(): FlashcardSet | null {
      return this.flashcardSets[0] ?? null
    },
    lastFlashcardSet(): FlashcardSet | null {
      return this.flashcardSets[this.flashcardSets.length - 1] ?? null
    },
  },
  actions: {
    async loadData(): Promise<void> {
      await apiClient.get<FlashcardSetsGetResponse>('/flashcard-sets').then(response => {
        this.flashcardSets = response.data.flashcardSets.sort((a, b) => {
          if (a.default && !b.default) return -1
          if (!a.default && b.default) return 1

          return a.name.localeCompare(b.name)
        })
      })
      // todo handle errors
    },
    async addFlashcardSet(flashcardSet: FlashcardSet): Promise<void> {
      const request: FlashcardSetsPostRequest = {
        flashcardSet: flashcardSet,
      }

      try {
        const response = await apiClient.post<FlashcardSetsPostResponse>('/flashcard-sets', request)
        this.flashcardSets.push(response.data.flashcardSet)
      } catch (error) {
        // todo handle errors
      }
    },
    async removeFlashcardSet(flashcardSet: FlashcardSet): Promise<void> {
      await apiClient.delete(`/flashcard-sets/${flashcardSet.id}`).then(_ => {
        const idx = this.flashcardSets.indexOf(flashcardSet, 0)
        if (idx > -1) {
          this.flashcardSets.splice(idx, 1)
        }
      })
      // todo handle errors
    },
  }
})
