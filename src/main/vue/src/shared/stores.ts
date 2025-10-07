import { useFlashcardSetsStore } from '@/stores/flashcard-sets-store.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { storeToRefs } from 'pinia'
import { FlashcardSet } from '@/model/flashcard.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import {
  sendChronoSyncRequest,
  sendFlashcardSetGetRequest,
  sendFlashcardsGetRequest
} from '@/api/api-client.ts'

export async function reloadFlashcardSetStores(): Promise<boolean> {
  console.log('Reloading flashcard set stores')
  const flashcardSetsStore = useFlashcardSetsStore()
  const flashcardSetStore = useFlashcardSetStore()

  const { firstFlashcardSet } = storeToRefs(flashcardSetsStore)

  if (firstFlashcardSet.value) {
    return await loadFlashcardSetStores(firstFlashcardSet.value)
  } else {
    flashcardSetStore.resetState()
    return true
  }
}

export async function loadFlashcardSetStores(flashcardSet: FlashcardSet): Promise<boolean> {
  console.log(`Loading flashcard set stores state for ${flashcardSet.id}`)
  const toaster = useSpaceToaster()
  const flashcardSetStore = useFlashcardSetStore()
  const chronoStore = useChronoStore()

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

export async function fullyLoadFlashcardSetStore(setId: number): Promise<boolean> {
  console.log(`Fully loading flashcard set stores for ${setId}`)
  const toaster = useSpaceToaster()

  return await sendFlashcardSetGetRequest(setId)
    .then((response) => {
      return loadFlashcardSetStores(response.data)
    })
    .catch((error) => {
      console.error(`Failed to get flashcard set ${setId}`, error)
      toaster.bakeError(`Flashcard set error`, error.response?.data)
      return false
    })
}
