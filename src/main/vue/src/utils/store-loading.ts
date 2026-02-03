import { watch } from 'vue'
import { FlashcardSet } from '@/model/flashcard.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import {
  loadSelectedSetIdFromCookies,
  removeSelectedSetIdCookie,
  saveSelectedSetIdToCookies,
} from '@/utils/cookies.ts'
import { useLanguageStore } from '@/stores/language-store.ts';
import { useSpaceToaster } from '@/stores/toast-store.ts';
import { Log, LogTag } from '@/utils/logger.ts';
import { sendLanguagesGetRequest } from '@/api/public-api-client.ts';
import { userApiErrors } from '@/api/user-api-error.ts';

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
 */
export function waitUntilStoreLoaded<T>(store: T, property: keyof T = 'loaded' as keyof T): Promise<void> {
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
