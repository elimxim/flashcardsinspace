import { watch } from 'vue'
import { FlashcardSet } from '@/model/flashcard.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import {
  loadSelectedSetIdFromCookies,
  removeSelectedSetIdCookie,
  saveSelectedSetIdToCookies,
} from '@/utils/cookies.ts'

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
