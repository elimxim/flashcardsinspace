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
    async addFlashcardSet(flashcardSet: FlashcardSet): Promise<FlashcardSet | null> {
      const request: FlashcardSetsPostRequest = {
        flashcardSet: flashcardSet,
      }

      try {
        const response = await apiClient.post<FlashcardSetsPostResponse>('/flashcard-sets', request)
        this.flashcardSets.push(response.data.flashcardSet)
        return response.data.flashcardSet
      } catch (error) {
        return null // fixme
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
