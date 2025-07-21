import { type FlashcardSet } from '@/models/flashcard.ts'
import { type User } from '@/models/user.ts'
import { defineStore } from 'pinia'
import type { FlashcardData } from '@/models/store.ts'
import { levels } from '@/core-logic/level-logic.ts';

export const useFlashcardDataStore = defineStore('flashcard-data', {
  state: (): FlashcardData => {
    return {
      flashcardSets: [],
    }
  },
  actions: {
    loadData() {
      this.flashcardSets = testData().sort((a, b) => {
        if (a.default && !b.default) return -1
        if (!a.default && b.default) return 1

        return a.name.localeCompare(b.name)
      })
    },
    addFlashcardSet(flashcardSet: FlashcardSet) {
      flashcardSet.id = nextId(this.flashcardSets)
      flashcardSet.createdAt = new Date().toISOString()
      flashcardSet.lastUpdatedAt = new Date().toISOString()
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

function nextId(flashcardSets: FlashcardSet[]): number {
  let max = 1
  if (flashcardSets.length > 0) {
    max = flashcardSets.reduce(
      (prev, curr) => {
        return (prev && prev.id > curr.id) ? prev : curr
      },
      flashcardSets[0]
    )?.id
  }
  return max + 1
}

function testEmptyData(): FlashcardSet[] {
  return []
}

function testData(): FlashcardSet[] {
  const user: User = {
    id: 1,
    name: "Billy Bob",
    registeredAt: new Date(),
  }

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
          level: levels.UNKNOWN.name,
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
          level: levels.ATTEMPTED.name,
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
          level: levels.FIRST.name,
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
          level: levels.SECOND.name,
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
