import { type FlashcardSet } from '@/model/flashcard.ts'
import { defineStore } from 'pinia'
import { stages } from '@/core-logic/stage-logic.ts'
import { asIsoDate, minusDays, today, yesterday } from '@/utils/date'

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
  return [
    {
      id: 1,
      name: "Bosnian",
      language: {
        name: "Bosnian",
        code: "bs",
      },
      flashcardMap: new Map([
        [1, {
          id: 1,
          frontSide: "Prdnuti",
          backSide: "To fart",
          stage: stages.FIRST.name, // unknown
          reviewCount: 0,
          reviewHistory: [], // todo
          reviewedAt: null,
          createdAt: asIsoDate(today()),
          lastUpdatedAt: asIsoDate(today()),
        }],
        [2, {
          id: 2,
          frontSide: "Kakati",
          backSide: "To poop",
          stage: stages.FIRST.name, // attempted
          reviewCount: 0,
          reviewHistory: [],
          reviewedAt: asIsoDate(today()),
          createdAt: asIsoDate(yesterday()),
          lastUpdatedAt: asIsoDate(today()),
        }],
      ]),
      createdAt: asIsoDate(yesterday()),
      lastUpdatedAt: asIsoDate(yesterday()),
      default: false,
    },
    {
      id: 2,
      name: "Russian",
      language: {
        name: "Russian",
        code: "ru",
      },
      flashcardMap: new Map([
        [1, {
          id: 1,
          frontSide: "Пукать",
          backSide: "To fart",
          stage: stages.FIRST.name,
          reviewCount: 0,
          reviewHistory: [],
          reviewedAt: asIsoDate(yesterday()),
          createdAt: asIsoDate(minusDays(today(), 2)),
          lastUpdatedAt: asIsoDate(minusDays(today(), 2)),
        }],
        [2, {
          id: 2,
          frontSide: "Какать",
          backSide: "To poop",
          stage: stages.SEVENTH.name,
          reviewCount: 0,
          reviewHistory: [],
          reviewedAt: asIsoDate(yesterday()),
          createdAt: asIsoDate(minusDays(today(), 64)),
          lastUpdatedAt: asIsoDate(yesterday()),
        }],
      ]),
      createdAt: asIsoDate(minusDays(today(), 64)),
      lastUpdatedAt: asIsoDate(minusDays(today(), 64)),
      default: false,
    }
  ]
}
