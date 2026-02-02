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
import { Log, LogTag } from '@/utils/logger.ts'
import { userApiErrors } from '@/api/user-api-error.ts'
import { watch } from 'vue'

export function determineCurrFlashcardSet(): FlashcardSet | undefined {
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
  const flashcardStore = useFlashcardStore()

  const currFlashcardSet = determineCurrFlashcardSet()
  Log.log(LogTag.STORE, `Current FlashcardSet.id=${currFlashcardSet?.id}`)
  if (currFlashcardSet) {
    return await loadFlashcardRelatedStores(currFlashcardSet, forced)
  } else {
    flashcardStore.resetState()
    return true
  }
}

export async function loadFlashcardRelatedStores(flashcardSet: FlashcardSet, forced: boolean = false): Promise<boolean> {
  const toaster = useSpaceToaster()
  const flashcardStore = useFlashcardStore()
  const chronoStore = useChronoStore()
  const audioStore = useAudioStore()

  const currSetId = flashcardStore.flashcardSet?.id
  if (flashcardStore.loaded && flashcardSet.id === currSetId && !forced) {
    Log.log(LogTag.STORE, `Stores are already loaded, FlashcardSet.id=${flashcardSet.id}`)
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
      Log.error(LogTag.STORE, `Failed to load store for FlashcardSet.id=${flashcardSet.id}`, error)
      toaster.bakeError(userApiErrors.DATA_LOADING, error.response?.data)
      return false
    })
}

export async function loadFlashcardRelatedStoresById(setId: number, forced: boolean = false): Promise<boolean> {
  const toaster = useSpaceToaster()

  return await sendFlashcardSetGetRequest(setId)
    .then((response) => {
      return loadFlashcardRelatedStores(response.data, forced)
    })
    .catch((error) => {
      Log.error(LogTag.STORE, `Failed to load store for FlashcardSet.id=${setId}`, error)
      toaster.bakeError(userApiErrors.DATA_LOADING, error.response?.data)
      return false
    })
}

export async function loadFlashcardSetStore(forced: boolean = false): Promise<boolean> {
  const flashcardSetStore = useFlashcardSetStore()
  const toaster = useSpaceToaster()

  if (flashcardSetStore.loaded && !forced) {
    Log.log(LogTag.STORE, 'flashcard-set store is already loaded')
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
      Log.error(LogTag.STORE, 'Failed to load flashcard-set store', error)
      toaster.bakeError(userApiErrors.DATA_LOADING, error.response?.data)
      return false
    })
}

export async function loadLanguageStore(): Promise<boolean> {
  const languageStore = useLanguageStore()
  const toaster = useSpaceToaster()

  if (languageStore.loaded) {
    Log.log(LogTag.STORE, 'language store is already loaded')
    return true
  }

  return await sendLanguagesGetRequest()
    .then(response => {
      languageStore.loadState(response.data)
      return true
    }).catch(error => {
      Log.error(LogTag.STORE, 'Failed to load language store', error)
      toaster.bakeError(userApiErrors.DATA_LOADING, error.response?.data)
      return false
    })
}

/**
 * Waits for a specific property on a Pinia store to become truthy.
 * @param store The Pinia store instance
 * @param property The boolean property to watch (defaults to 'loaded')
 */
export function waitUntilLoaded<T>(store: T, property: keyof T = 'loaded' as keyof T): Promise<void> {
  if (store[property]) {
    return Promise.resolve()
  }

  return new Promise((resolve) => {
    const unwatch = watch(
      () => store[property],
      (value) => {
        if (value) {
          unwatch()
          resolve()
        }
      }
    )
  })
}
