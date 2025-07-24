import { type Flashcard, type FlashcardSet } from '@/models/flashcard.ts';
import { defineStore } from 'pinia';
import type { FlashcardSetState } from '@/models/state.ts';

export const useFlashcardStateStore = defineStore('flashcard-state', {
  state: (): FlashcardSetState => {
    return {
      flashcardSet: null,
    }
  },
  getters: {
    flashcards(): Flashcard[] {
      if (this.flashcardSet !== null) {
        return [...this.flashcardSet.flashcardMap.values()]
      } else {
        return []
      }
    },
  },
  actions: {
    init(flashcardSet: FlashcardSet) {
      this.flashcardSet = flashcardSet
    },
    initFromList(flashcardSets: FlashcardSet[]) {
      this.flashcardSet = flashcardSets[0] ?? null
    },
    setName(value: string) {
      if (this.flashcardSet !== null) {
        this.flashcardSet.name = value
        this.flashcardSet.lastUpdatedAt = new Date().toISOString()
      } else {
        throw new Error(`Can't set name=${value}: flashcard set is null`)
      }
    },
    setDefault(value: boolean) {
      if (this.flashcardSet !== null) {
        this.flashcardSet.default = value
        this.flashcardSet.lastUpdatedAt = new Date().toISOString()
      } else {
        throw new Error(`Can't set default=${value}: flashcard set is null`)
      }
    },
    addFlashcard(flashcard: Flashcard) {
      if (this.flashcardSet !== null) {
        flashcard.id = nextId(this.flashcardSet.flashcardMap)
        this.flashcardSet.flashcardMap.set(flashcard.id, flashcard)
      } else {
        console.error(`Can't add flashcard ${flashcard}: flashcard set is null`)
      }
    },
    updateFlashcard(value: Flashcard) {
      if (this.flashcardSet !== null) {
        const flashcard = this.flashcardSet.flashcardMap.get(value.id) as Flashcard | undefined
        if (flashcard !== undefined) {
          flashcard.frontSide = value.frontSide
          flashcard.backSide = value.backSide
          flashcard.stage = value.stage
          flashcard.reviewCount = value.reviewCount
          flashcard.reviewHistory = value.reviewHistory
          flashcard.lastUpdatedAt = value.lastUpdatedAt
        }
      } else {
        throw new Error(`Can't update flashcard=${value}: flashcard set is null`)
      }
    },
    removeFlashcard(id: number) {
      if (this.flashcardSet !== null) {
        this.flashcardSet.flashcardMap.delete(id)
      } else {
        throw new Error(`Can't remove flashcard with id=${id}: flashcard set is null`)
      }
    },
  }
})

// fixme for testing purpose
function nextId(flashcardMap: Map<number, Flashcard>): number {
  let max = 1
  if (flashcardMap.size) {
    const keys = Array.from(flashcardMap.keys())
    max = Math.max(...keys)
  }
  return max + 1
}
