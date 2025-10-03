import { type FlashcardSet } from '@/model/flashcard.ts'
import { defineStore } from 'pinia'
import apiClient from '@/api/api-client.ts'

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
    firstFlashcardSet(): FlashcardSet | null {
      return this.flashcardSets[0] ?? null
    },
    lastFlashcardSet(): FlashcardSet | null {
      return this.flashcardSets[this.flashcardSets.length - 1] ?? null
    },
  },
  actions: {
    async loadFlashcardSets(): Promise<void> {
      await apiClient.get<FlashcardSet[]>('/flashcard-sets').then(response => {
        this.flashcardSets = response.data.sort((a, b) => {
          if (a.default && !b.default) return -1
          if (!a.default && b.default) return 1

          return a.name.localeCompare(b.name)
        })
        this.loaded = true
      })
      // todo handle errors
    },
    async addFlashcardSet(flashcardSet: FlashcardSet): Promise<void> {
      const response = await apiClient.post<FlashcardSet>('/flashcard-sets', flashcardSet)
      this.flashcardSets.push(response.data)
      // todo handle errors
    },
    async removeFlashcardSet(flashcardSet: FlashcardSet): Promise<void> {
      await apiClient.delete(`/flashcard-sets/${flashcardSet.id}`).then(() => {
        const idx = this.flashcardSets.indexOf(flashcardSet, 0)
        if (idx > -1) {
          this.flashcardSets.splice(idx, 1)
        }
      })
      // todo handle errors
    },
    overrideFlashcardSet(flashcardSet: FlashcardSet) {
      const idx = this.flashcardSets.findIndex((v) => v.id == flashcardSet.id)
      if (idx !== -1) {
        this.flashcardSets[idx] = flashcardSet
      }
    },
  }
})
