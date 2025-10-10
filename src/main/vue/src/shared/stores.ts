import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
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
  const flashcardSetStore = useFlashcardSetStore()

  let flashcardSet
  const selectedSetId = loadSelectedSetId()
  if (selectedSetId) {
    flashcardSet = flashcardSetStore.findSet(selectedSetId)
    if (flashcardSet) {
      return flashcardSet
    } else {
      return flashcardSetStore.firstFlashcardSet
    }
  }
}

export async function reloadFlashcardAndChronoStores(forced: boolean = false): Promise<boolean> {
  console.log('Reloading flashcard and chrono stores')
  const flashcardStore = useFlashcardStore()

  const currFlashcardSet = determineCurrFlashcardSet()
  if (currFlashcardSet) {
    return await loadFlashcardAndChronoStores(currFlashcardSet, forced)
  } else {
    flashcardStore.resetState()
    return true
  }
}

export async function loadFlashcardAndChronoStores(flashcardSet: FlashcardSet, forced: boolean = false): Promise<boolean> {
  console.log(`Loading flashcard and chrono stores for ${flashcardSet.id}, forced: ${forced}`)
  const toaster = useSpaceToaster()
  const flashcardStore = useFlashcardStore()
  const chronoStore = useChronoStore()

  const currSetId = flashcardStore.flashcardSet?.id
  if (flashcardStore.loaded && flashcardSet.id === currSetId && !forced) {
    console.log(`Flashcard set ${flashcardSet.id} already loaded, skipping`)
    return true
  }

  return await sendFlashcardsGetRequest(flashcardSet.id)
    .then(async (response) => {
      flashcardStore.loadState(flashcardSet, response.data)
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

export async function loadFlashcardAndChronoStoresById(setId: number, forced: boolean = false): Promise<boolean> {
  console.log(`Loading flashcard and chrono stores for ${setId}, forced: ${forced}`)
  const toaster = useSpaceToaster()

  return await sendFlashcardSetGetRequest(setId)
    .then((response) => {
      return loadFlashcardAndChronoStores(response.data, forced)
    })
    .catch((error) => {
      console.error(`Failed to get flashcard set ${setId}`, error)
      toaster.bakeError(`Flashcard set error`, error.response?.data)
      return false
    })
}

export async function loadFlashcardSetStore(forced: boolean = false): Promise<boolean> {
  console.log(`Loading flashcard set store, forced: ${forced}`)
  const flashcardSetStore = useFlashcardSetStore()
  const toaster = useSpaceToaster()

  if (flashcardSetStore.loaded && !forced) {
    console.log('Flashcard set store already loaded, skipping')
    return true
  }

  return await sendFlashcardSetListGetRequest()
    .then((response) => {
      flashcardSetStore.loadState(sortFlashcardSets(response.data))
      return true
    })
    .catch((error) => {
      console.error(`Failed to load flashcard sets`, error)
      toaster.bakeError(`Flashcard sets error`, error.response?.data)
      return false
    })
}
