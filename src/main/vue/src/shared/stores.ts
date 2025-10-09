import { useFlashcardSetsStore } from '@/stores/flashcard-sets-store.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { FlashcardSet } from '@/model/flashcard.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import {
  sendChronoSyncRequest,
  sendFlashcardSetGetRequest,
  sendFlashcardSetListGetRequest,
  sendFlashcardsGetRequest
} from '@/api/api-client.ts'
import { sortFlashcardSets } from '@/core-logic/flashcard-logic.ts'
import { loadSelectedSetId } from '@/shared/cookies.ts'

export function determineCurrFlashcardSet(): FlashcardSet | undefined {
  console.log('Determining current flashcard set')
  const flashcardSetsStore = useFlashcardSetsStore()

  let flashcardSet
  const selectedSetId = loadSelectedSetId()
  if (selectedSetId) {
    flashcardSet = flashcardSetsStore.findSet(selectedSetId)
    if (flashcardSet) {
      return flashcardSet
    } else {
      return flashcardSetsStore.firstFlashcardSet
    }
  }
}

export async function reloadFlashcardSetAndChronoStores(forced: boolean = false): Promise<boolean> {
  console.log('Reloading flashcard set stores')
  const flashcardSetStore = useFlashcardSetStore()

  const currFlashcardSet = determineCurrFlashcardSet()
  if (currFlashcardSet) {
    return await loadFlashcardSetAndChronoStores(currFlashcardSet, forced)
  } else {
    flashcardSetStore.resetState()
    return true
  }
}

export async function loadFlashcardSetAndChronoStores(flashcardSet: FlashcardSet, forced: boolean = false): Promise<boolean> {
  console.log(`Loading flashcard set and chrono stores for ${flashcardSet.id}, forced: ${forced}`)
  const toaster = useSpaceToaster()
  const flashcardSetStore = useFlashcardSetStore()
  const chronoStore = useChronoStore()

  const currSetId = flashcardSetStore.flashcardSet?.id
  if (flashcardSetStore.loaded && flashcardSet.id === currSetId && !forced) {
    console.log(`Flashcard set ${flashcardSet.id} already loaded, skipping`)
    return true
  }

  return await sendFlashcardsGetRequest(flashcardSet.id)
    .then(async (response) => {
      flashcardSetStore.loadState(flashcardSet, response.data)
      return sendChronoSyncRequest(flashcardSet.id)
    })
    .then((response) => {
      chronoStore.loadState(
        response.data.chronodays,
        response.data.currDay,
      )
      return true
    })
    .catch((error) => {
      console.error(`Failed to load flashcard set ${flashcardSet.id}`, error)
      toaster.bakeError(`Flashcard set error`, error.response?.data)
      return false
    })
}

export async function loadFlashcardSetAndChronoStoresById(setId: number, forced: boolean = false): Promise<boolean> {
  console.log(`Loading flashcard set and chrono stores for ${setId}, forced: ${forced}`)
  const toaster = useSpaceToaster()

  return await sendFlashcardSetGetRequest(setId)
    .then((response) => {
      return loadFlashcardSetAndChronoStores(response.data, forced)
    })
    .catch((error) => {
      console.error(`Failed to get flashcard set ${setId}`, error)
      toaster.bakeError(`Flashcard set error`, error.response?.data)
      return false
    })
}

export async function loadFlashcardSetsStore(forced: boolean = false): Promise<boolean> {
  console.log(`Loading flashcard sets store, forced: ${forced}`)
  const flashcardSetsStore = useFlashcardSetsStore()
  const toaster = useSpaceToaster()

  if (flashcardSetsStore.loaded && !forced) {
    console.log('Flashcard sets store already loaded, skipping')
    return true
  }

  return await sendFlashcardSetListGetRequest()
    .then((response) => {
      flashcardSetsStore.loadState(sortFlashcardSets(response.data))
      return true
    })
    .catch((error) => {
      console.error(`Failed to load flashcard sets`, error)
      toaster.bakeError(`Flashcard sets error`, error.response?.data)
      return false
    })
}
