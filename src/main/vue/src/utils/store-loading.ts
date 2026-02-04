import { watch } from 'vue'
import { FlashcardSet } from '@/model/flashcard.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import {
  loadSelectedSetIdFromCookies,
  removeSelectedSetIdCookie,
  saveSelectedSetIdToCookies,
} from '@/utils/cookies.ts'
import { useLanguageStore } from '@/stores/language-store.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { Log, LogTag } from '@/utils/logger.ts'
import { sendLanguagesGetRequest } from '@/api/public-api-client.ts'
import { userApiErrors } from '@/api/user-api-error.ts'
import {
  sendChronoSyncRequest,
  sendFlashcardAudioMetadataGetRequest,
  sendFlashcardSetExtraRequest, sendFlashcardSetGetRequest,
  sendFlashcardSetsGetRequest,
  sendFlashcardsGetRequest,
} from '@/api/api-client.ts'
import { sortFlashcardSets } from '@/core-logic/flashcard-logic.ts'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { useAudioStore } from '@/stores/audio-store.ts'

export function getCurrFlashcardSet(): FlashcardSet | undefined {
  const flashcardSetStore = useFlashcardSetStore()
  const selectedSetId = loadSelectedSetIdFromCookies()
  if (selectedSetId) {
    const flashcardSet = flashcardSetStore.findSet(selectedSetId)
    if (flashcardSet) {
      return flashcardSet
    } else {
      const firstSet = flashcardSetStore.firstFlashcardSet
      if (firstSet) {
        saveSelectedSetIdToCookies(firstSet.id)
      } else {
        removeSelectedSetIdCookie()
      }
    }
  }
}

/**
 * Waits for a specific property on a Pinia store to become truthy.
 * @param store The Pinia store instance
 * @param property The boolean property to watch (defaults to 'loaded')
 * @param timeout The timeout in milliseconds (defaults to 10000)
 */
export function waitUntilStoreLoaded<T>(
  store: T,
  property: keyof T = 'loaded' as keyof T,
  timeout: number = 10000,
): Promise<void> {
  if (store[property]) {
    return Promise.resolve()
  }

  return new Promise((resolve, reject) => {
    const timer = setTimeout(() => {
      unwatch()
      if (!store[property]) {
        reject(new Error(`Store property ${String(property)} timed out after ${timeout}ms`))
      }
    }, timeout)

    const unwatch = watch(
      () => store[property],
      (value) => {
        if (value) {
          clearTimeout(timer)
          unwatch()
          resolve()
        }
      },
      { immediate: true },
    )
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

export async function loadFlashcardSetStore(): Promise<boolean> {
  const flashcardSetStore = useFlashcardSetStore()
  const toaster = useSpaceToaster()

  if (flashcardSetStore.loaded) {
    Log.log(LogTag.STORE, 'flashcard-set store is already loaded')
    return true
  }

  return await sendFlashcardSetsGetRequest()
    .then(async (response) => {
      flashcardSetStore.loadState(sortFlashcardSets(response.data))
      return true
    })
    .catch((error) => {
      Log.error(LogTag.STORE, 'Failed to load flashcard-set store', error)
      toaster.bakeError(userApiErrors.DATA_LOADING, error.response?.data)
      return false
    })
}

export async function loadFlashcardSetExtras(): Promise<boolean> {
  const flashcardSetStore = useFlashcardSetStore()
  const toaster = useSpaceToaster()

  return await sendFlashcardSetExtraRequest()
    .then((response) => {
      flashcardSetStore.loadExtras(response.data)
      return true
    })
    .catch((error) => {
      Log.error(LogTag.STORE, 'Failed to load flashcard set extra', error)
      toaster.bakeError(userApiErrors.DATA_LOADING, error.response?.data)
      return false
    })
}

export async function loadStoresForFlashcardSet(flashcardSet: FlashcardSet, forced: boolean = false): Promise<boolean> {
  const toaster = useSpaceToaster()
  const flashcardStore = useFlashcardStore()
  const chronoStore = useChronoStore()
  const audioStore = useAudioStore()

  const currSetId = flashcardStore.flashcardSet?.id
  if (flashcardStore.loaded && flashcardSet.id === currSetId && !forced) {
    Log.log(LogTag.STORE, `flashcard store is already loaded`)
    return true
  }

  flashcardStore.resetState()
  chronoStore.resetState()
  audioStore.resetState()

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
      Log.error(LogTag.STORE, `Failed to load stores for FlashcardSet.id=${flashcardSet.id}`, error)
      toaster.bakeError(userApiErrors.DATA_LOADING, error.response?.data)
      return false
    })
}

export async function loadStoresForCurrFlashcardSet(forced: boolean = false): Promise<boolean> {
  const flashcardStore = useFlashcardStore()

  const currFlashcardSet = getCurrFlashcardSet()
  Log.log(LogTag.STORE, `Current FlashcardSet.id=${currFlashcardSet?.id}`)
  if (currFlashcardSet) {
    return await loadStoresForFlashcardSet(currFlashcardSet, forced)
  } else {
    flashcardStore.resetState()
    return true
  }
}

export async function loadStoresForFlashcardSetId(setId: number, forced: boolean = false): Promise<boolean> {
  const toaster = useSpaceToaster()

  return await sendFlashcardSetGetRequest(setId)
    .then((response) => {
      return loadStoresForFlashcardSet(response.data, forced)
    })
    .catch((error) => {
      Log.error(LogTag.STORE, `Failed to load stores for FlashcardSet.id=${setId}`, error)
      toaster.bakeError(userApiErrors.DATA_LOADING, error.response?.data)
      return false
    })
}
