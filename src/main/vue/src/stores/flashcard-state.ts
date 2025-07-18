import { type Flashcard, type FlashcardSet } from "@/models/flashcard.ts";
import { defineStore } from "pinia";
import type { Ref } from 'vue';

export const useFlashcardStateStore = defineStore('flashcard-state', {
  state: () => {
    return {
      currFlashcardSet: undefined as FlashcardSet | undefined,
    }
  },
  getters: {
    flashcards(): Flashcard[] {
      if (this.currFlashcardSet !== undefined) {
        return [...this.currFlashcardSet.flashcardMap.values()]
      } else {
        return []
      }
    }
  },
  actions: {
    chooseCurr(flashcardSets: Ref<FlashcardSet[]>) {
      // todo alpha-bet search + default flag
      this.currFlashcardSet = flashcardSets.value?.[0]
    },
    setCurr(flashcardSet: FlashcardSet) {
      this.currFlashcardSet = flashcardSet
    },
    setName(name: string) {
      if (this.currFlashcardSet !== undefined) {
        this.currFlashcardSet.name = name
      }
    },
    setDefault(value: boolean) {
      if (this.currFlashcardSet !== undefined) {
        this.currFlashcardSet.default = value
      }
    },
    addFlashcard(flashcard: Flashcard) {
      if (this.currFlashcardSet !== undefined) {
        // todo delete
        let nextId = 1
        if (this.currFlashcardSet.flashcardMap.size) {
          const keys = Array.from(this.currFlashcardSet.flashcardMap.keys())
          nextId = Math.max(...keys) + 1
        }
        // todo delete
        flashcard.id = nextId

        // todo save to DB and get ID from there
        this.currFlashcardSet.flashcardMap.set(nextId, flashcard)
      } else {
        console.error("Cannot add flashcard to undefined flashcard set")
      }
    },
    updateFlashcard(flashcard: Flashcard) {
      if (this.currFlashcardSet !== undefined) {
        const savedFlashcard = this.currFlashcardSet.flashcardMap.get(flashcard.id) as Flashcard | undefined
        if (savedFlashcard !== undefined) {
          savedFlashcard.frontSide = flashcard.frontSide
          savedFlashcard.backSide = flashcard.backSide
          savedFlashcard.level = flashcard.level
          savedFlashcard.lastUpdatedAt = new Date()
        }
      }
    },
  }
})
