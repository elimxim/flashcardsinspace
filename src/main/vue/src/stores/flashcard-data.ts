import { type FlashcardSet, Level } from '@/models/flashcards.ts';
import { type User } from '@/models/users.ts';
import { defineStore } from 'pinia'

export const useFlashcardDataStore = defineStore('flashcard-data', {
  state: () => {
    return {
      flashcardSets: [] as FlashcardSet[],
    }
  },
  actions: {
    loadFlashcards() {
      this.flashcardSets = testFlashcardData()
      return this.flashcardSets
    },
    addFlashcardSet(flashcardSet: FlashcardSet) {
      // todo delete
      let max = 1
      if (this.flashcardSets.length > 0) {
        max = this.flashcardSets.reduce(
          (prev, current) => {
            return (prev && prev.id > current.id) ? prev : current;
          },
          this.flashcardSets[0]
        )?.id
      }

      // todo delete
      flashcardSet.id = max + 1

      this.flashcardSets.push(flashcardSet)
    },
    removeFlashcardSet(flashcardSet: FlashcardSet) {
      const idx = this.flashcardSets.indexOf(flashcardSet, 0)
      if (idx > -1) {
        this.flashcardSets.splice(idx, 1)
      }
    },
  }
})

export const testEmptyFlashcardData = () => {
  return [] as FlashcardSet[]
}

export const testFlashcardData = () => {
  const user: User = {
    id: 1,
    name: "Billy Bob",
    registeredAt: new Date(),
  };

  return [
    {
      id: 1,
      name: "Bosnian",
      user: user,
      targetLanguage: {
        code: "bs",
        name: "Bosnian",
      },
      flashcards: [
        {
          id: 1,
          frontSide: "Prdnuti",
          backSide: "To fart",
          level: Level.FIRST,
          createdAt: new Date(),
          lastUpdatedAt: new Date(),
        },
        {
          id: 1,
          frontSide: "Kakati",
          backSide: "To poop",
          level: Level.FIRST,
          createdAt: new Date(),
          lastUpdatedAt: new Date(),
        },
      ],
      createdAt: new Date(),
      lastUpdatedAt: new Date(),
    },
    {
      id: 2,
      user: user,
      name: "Russian",
      targetLanguage: {
        code: "ru",
        name: "Russian",
      },
      flashcards: [
        {
          id: 1,
          frontSide: "Пукать",
          backSide: "To fart",
          level: Level.SECOND,
          createdAt: new Date(),
          lastUpdatedAt: new Date(),
        },
        {
          id: 1,
          frontSide: "Какать",
          backSide: "To poop",
          level: Level.SECOND,
          createdAt: new Date(),
          lastUpdatedAt: new Date(),
        },
      ],
      createdAt: new Date(),
      lastUpdatedAt: new Date(),
    }
  ]
}
