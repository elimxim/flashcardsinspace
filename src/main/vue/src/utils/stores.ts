import { FlashcardSet } from '@/model/flashcard.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { useAudioStore } from '@/stores/audio-store.ts'
import {
  sendChronoSyncRequest,
  sendFlashcardAudioMetadataGetRequest,
  sendFlashcardSetExtraRequest,
  sendFlashcardSetGetRequest,
  sendFlashcardSetsGetRequest,
  sendFlashcardsGetRequest,
} from '@/api/api-client.ts'
import { loadSelectedSetIdFromCookies } from '@/utils/cookies.ts'
import { sortFlashcardSets } from '@/core-logic/flashcard-logic.ts'
import { sendLanguagesGetRequest } from '@/api/public-api-client.ts'
import { useLanguageStore } from '@/stores/language-store.ts'

export function determineCurrFlashcardSet(): FlashcardSet | undefined {
  console.log('Determining current flashcard set')
  const flashcardSetStore = useFlashcardSetStore()

  let flashcardSet
  const selectedSetId = loadSelectedSetIdFromCookies()
  if (selectedSetId) {
    flashcardSet = flashcardSetStore.findSet(selectedSetId)
    if (flashcardSet) {
      return flashcardSet
    } else {
      return flashcardSetStore.firstFlashcardSet
    }
  }
}

export async function reloadFlashcardRelatedStores(forced: boolean = false): Promise<boolean> {
  console.log('Reloading flashcard related stores')
  const flashcardStore = useFlashcardStore()

  const currFlashcardSet = determineCurrFlashcardSet()
  if (currFlashcardSet) {
    return await loadFlashcardRelatedStores(currFlashcardSet, forced)
  } else {
    flashcardStore.resetState()
    return true
  }
}

export async function loadFlashcardRelatedStores(flashcardSet: FlashcardSet, forced: boolean = false): Promise<boolean> {
  console.log(`Loading flashcard related stores for ${flashcardSet.id}, forced: ${forced}`)
  const toaster = useSpaceToaster()
  const flashcardStore = useFlashcardStore()
  const chronoStore = useChronoStore()
  const audioStore = useAudioStore()

  const currSetId = flashcardStore.flashcardSet?.id
  if (flashcardStore.loaded && flashcardSet.id === currSetId && !forced) {
    console.log(`Flashcard store for flashcard set id ${flashcardSet.id} is already loaded`)
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
        response.data.dayStreak,
      )
      return sendFlashcardAudioMetadataGetRequest(flashcardSet.id)
    })
    .then((response) => {
      audioStore.loadState(response.data)
      return true
    })
    .catch((error) => {
      console.error(`Failed to load flashcard set ${flashcardSet.id}`, error)
      toaster.bakeError(`Data fetching error`, error.response?.data)
      return false
    })
}

export async function loadFlashcardRelatedStoresById(setId: number, forced: boolean = false): Promise<boolean> {
  console.log(`Loading flashcard related stores for ${setId}, forced: ${forced}`)
  const toaster = useSpaceToaster()

  return await sendFlashcardSetGetRequest(setId)
    .then((response) => {
      return loadFlashcardRelatedStores(response.data, forced)
    })
    .catch((error) => {
      console.error(`Failed to get flashcard set ${setId}`, error)
      toaster.bakeError(`Data fetching error`, error.response?.data)
      return false
    })
}

export async function loadFlashcardSetStore(forced: boolean = false): Promise<boolean> {
  console.log(`Loading flashcard set store, forced: ${forced}`)
  const flashcardSetStore = useFlashcardSetStore()
  const toaster = useSpaceToaster()

  if (flashcardSetStore.loaded && !forced) {
    console.log('Flashcard set store is already loaded')
    return true
  }

  return await sendFlashcardSetsGetRequest()
    .then(async (response) => {
      return sendFlashcardSetExtraRequest()
        .then((extraResponse) => {
          flashcardSetStore.loadState(sortFlashcardSets(response.data), extraResponse.data)
          return true
        })
    })
    .catch((error) => {
      console.error(`Failed to load flashcard sets`, error)
      toaster.bakeError(`Data fetching error`, error.response?.data)
      return false
    })
}

export async function loadLanguageStore(): Promise<boolean> {
  console.log('Loading language store')

  const languageStore = useLanguageStore()
  const toaster = useSpaceToaster()

  if (languageStore.loaded) {
    console.log('Language store is already loaded')
    return true
  }

  return await sendLanguagesGetRequest()
    .then(response => {
      languageStore.loadState(response.data)
      return true
    }).catch(error => {
      console.error('Error loading languages:', error)
      toaster.bakeError(`Data fetching error`, error.response?.data)
      return false
    })
}
