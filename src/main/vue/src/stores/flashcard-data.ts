import { type FlashcardSet, Level } from '@/models/flashcard.ts';
import { type User } from '@/models/user.ts';
import { defineStore } from 'pinia'

export const useFlashcardDataStore = defineStore('flashcard-data', {
  state: () => {
    return {
      flashcardSets: [] as FlashcardSet[],
    }
  },
  actions: {
    loadFlashcards() {
      // todo get from DB
      this.flashcardSets = testData().sort((a, b) => {
        if (a.default && !b.default) return -1;
        if (!a.default && b.default) return 1;

        return a.name.localeCompare(b.name)
      })
    },
    addFlashcardSet(flashcardSet: FlashcardSet) {
      // todo delete
      let max = 1
      if (this.flashcardSets.length > 0) {
        max = this.flashcardSets.reduce(
          (prev, curr) => {
            return (prev && prev.id > curr.id) ? prev : curr;
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

function testEmptyData(): FlashcardSet[] {
  return []
}

function testData(): FlashcardSet[] {
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
      language: {
        name: "Bosnian",
        alpha2: "bs",
      },
      flashcardMap: new Map([
        [1, {
          id: 1,
          frontSide: "Prdnuti",
          backSide: "To fart",
          level: Level.FIRST,
          reviewCount: 0,
          reviewHistory: [], // todo
          reviewedAt: null,
          createdAt: new Date().toISOString(),
          lastUpdatedAt: new Date().toISOString(),
        }],
        [2, {
          id: 2,
          frontSide: "Kakati",
          backSide: "To poop",
          level: Level.FIRST,
          reviewCount: 0,
          reviewHistory: [],
          reviewedAt: null,
          createdAt: new Date().toISOString(),
          lastUpdatedAt: new Date().toISOString(),
        }],
      ]),
      createdAt: new Date().toISOString(),
      lastUpdatedAt: new Date().toISOString(),
      default: false,
    },
    {
      id: 2,
      user: user,
      name: "Russian",
      language: {
        name: "Russian",
        alpha2: "ru",
      },
      flashcardMap: new Map([
        [1, {
          id: 1,
          frontSide: "Пукать",
          backSide: "To fart",
          level: Level.SECOND,
          reviewCount: 0,
          reviewHistory: [],
          reviewedAt: null,
          createdAt: new Date().toISOString(),
          lastUpdatedAt: new Date().toISOString(),
        }],
        [2, {
          id: 2,
          frontSide: "Какать",
          backSide: "To poop",
          level: Level.SECOND,
          reviewCount: 0,
          reviewHistory: [],
          reviewedAt: null,
          createdAt: new Date().toISOString(),
          lastUpdatedAt: new Date().toISOString(),
        }],
      ]),
      createdAt: new Date().toISOString(),
      lastUpdatedAt: new Date().toISOString(),
      default: false,
    }
  ]
}
