import { type Flashcard, type FlashcardSet, type ReviewInfo } from '@/model/flashcard.ts'
import { type Stage, stages } from '@/core-logic/stage-logic.ts'
import type { Language } from '@/model/language.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { storeToRefs } from 'pinia'
import { sendFlashcardSetGetRequest, sendFlashcardsGetRequest } from '@/api/api-client.ts'
import { useFlashcardSetsStore } from '@/stores/flashcard-sets-store.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'

export const flashcardSetStatuses = {
  ACTIVE: 'ACTIVE',
  DELETED: 'DELETED',
  SUSPENDED: 'SUSPENDED',
}

export function newFlashcard(frontSide: string, backSide: string): Flashcard {
  const chronoStore = useChronoStore()
  const { currDay } = storeToRefs(chronoStore)

  return {
    id: 0,
    frontSide: frontSide,
    backSide: backSide,
    stage: stages.S1.name,
    reviewedAt: null,
    reviewCount: 0,
    reviewHistory: { history: [] },
    createdAt: currDay.value.chronodate,
    lastUpdatedAt: null,
  }
}

export function updateFlashcardSides(flashcard: Flashcard, frontSide: string, backSide: string): Flashcard {
  flashcard.frontSide = frontSide
  flashcard.backSide = backSide
  flashcard.lastUpdatedAt = new Date().toISOString()
  return flashcard
}

export function updateFlashcard(flashcard: Flashcard, stage: Stage): Flashcard {
  const chronoStore = useChronoStore()
  const { currDay } = storeToRefs(chronoStore)

  const reviewedAt = currDay.value.chronodate

  const info: ReviewInfo = {
    stage: stage.name,
    reviewedAt: reviewedAt,
  }

  flashcard.stage = stage.name
  flashcard.reviewedAt = reviewedAt
  flashcard.reviewCount += 1
  flashcard.reviewHistory.history.push(info)
  flashcard.lastUpdatedAt = new Date().toISOString()
  return flashcard
}

export function createFlashcardSet(
  name: string,
  language: Language,
  first: boolean = false,
): FlashcardSet {
  return {
    id: 0,
    name: name,
    status: flashcardSetStatuses.ACTIVE,
    default: first,
    languageId: language.id,
    createdAt: new Date(),
    startedAt: null,
    lastUpdatedAt: null,
  }
}

export async function reloadFlashcardSetStore(): Promise<boolean> {
  console.log('Reloading flashcard set store')
  const flashcardSetsStore = useFlashcardSetsStore()
  const flashcardSetStore = useFlashcardSetStore()

  const { firstFlashcardSet } = storeToRefs(flashcardSetsStore)

  if (firstFlashcardSet.value) {
    return await loadFlashcardSetStore(firstFlashcardSet.value)
  } else {
    flashcardSetStore.resetState()
    return true
  }
}

export async function loadFlashcardSetStore(flashcardSet: FlashcardSet): Promise<boolean> {
  console.log(`Loading flashcard set store state for ${flashcardSet.id}`)
  const toaster = useSpaceToaster()
  const flashcardSetStore = useFlashcardSetStore()

  return await sendFlashcardsGetRequest(flashcardSet.id)
    .then((response) => {
      flashcardSetStore.loadState(flashcardSet, response.data)
      return true
    })
    .catch((error) => {
      console.error(`Failed to load flashcard set ${flashcardSet.id}`, error)
      toaster.bakeError(`Flashcard set error`, error.response?.data)
      return false
    })
}

export async function fullyLoadFlashcardSetStore(setId: number): Promise<boolean> {
  console.log(`Fully loading flashcard set store state for ${setId}`)
  const toaster = useSpaceToaster()

  return await sendFlashcardSetGetRequest(setId)
    .then((response) => {
      return loadFlashcardSetStore(response.data)
    })
    .catch((error) => {
      console.error(`Failed to get flashcard set ${setId}`, error)
      toaster.bakeError(`Flashcard set error`, error.response?.data)
      return false
    })
}

