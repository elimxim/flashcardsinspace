import { type Flashcard, type FlashcardSet } from "@/models/flashcards.ts";
import { type FlashcardState } from '@/models/state.ts';
import { defineStore } from "pinia";
import type { Ref } from 'vue';

export const useFlashcardStateStore = defineStore('flashcard-state', {
  state: (): FlashcardState => {
    return {
      currFlashcardSet: undefined,
    }
  },
  getters: {
    definedCurrFlashcardSet(): FlashcardSet { // can be undefined!!! the code needs to be changed
      if (this.currFlashcardSet === undefined) {
        throw new Error("Current flashcard set is undefined")
      }
      return this.currFlashcardSet
    },
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
