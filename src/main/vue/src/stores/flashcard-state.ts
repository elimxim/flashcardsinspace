import { type Flashcard, type FlashcardSet } from "@/models/flashcard.ts";
import { type FlashcardState } from '@/models/flashcard-state.ts';
import { defineStore } from "pinia";
import type { Ref } from 'vue';

export const useFlashcardStateStore = defineStore('flashcard-state', {
  state: (): FlashcardState => {
    return {
      currFlashcardSet: undefined,
    }
  },
  actions: {
    chooseCurrFlashcardSet(flashcardSets: Ref<FlashcardSet[]>) {
      this.currFlashcardSet = flashcardSets.value?.[0]
    },
    setCurrFlashcardSet(flashcardSet: FlashcardSet) {
      this.currFlashcardSet = flashcardSet
    },
    setFlashcardSetName(name: string) {
      if (this.currFlashcardSet !== undefined) {
        this.currFlashcardSet.name = name
      }
    },
    setFlashcardSetDefault(value: boolean) {
      if (this.currFlashcardSet !== undefined) {
        this.currFlashcardSet.default = value
      }
    },
    addFlashcard(flashcard: Flashcard) {
      if (this.currFlashcardSet !== undefined) {
        // todo delete
        let max = 1
        if (this.currFlashcardSet.flashcards.length > 0) {
          max = this.currFlashcardSet.flashcards.reduce(
            (prev, current) => {
              return (prev && prev.id > current.id) ? prev : current;
            },
            this.currFlashcardSet.flashcards[0]
          ).id
        }
        // todo delete
        flashcard.id = max + 1

        this.currFlashcardSet.flashcards.push(flashcard)
      } else {
        console.error("Cannot add flashcard to undefined flashcard set")
      }
    },
  }
})
